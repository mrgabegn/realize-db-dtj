package realize.db.dtj.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import realize.db.dtj.domain.Account;
import realize.db.dtj.domain.Movement;
import realize.db.dtj.domain.Transfer;
import realize.db.dtj.dto.event.TransferCompletedEvent;
import realize.db.dtj.dto.request.TransferRequest;
import realize.db.dtj.dto.response.TransferResponse;
import realize.db.dtj.repository.AccountRepository;
import realize.db.dtj.repository.MovementRepository;
import realize.db.dtj.repository.TransferRepository;
import realize.db.dtj.utils.enumerations.MovementType;
import realize.db.dtj.utils.exceptions.AccountNotFoundException;
import realize.db.dtj.utils.exceptions.InsufficientBalanceException;
import realize.db.dtj.utils.exceptions.InvalidTransferException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Test
    void shouldTransferSuccessfully() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account fromAccount = new Account(
                fromAccountId,
                "Origem",
                new BigDecimal("1000.00")
        );

        Account toAccount = new Account(
                toAccountId,
                "Destino",
                new BigDecimal("500.00")
        );

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("150.00")
        );

        when(accountRepository.findAllByIdWithLock(anyList()))
                .thenReturn(List.of(fromAccount, toAccount));

        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response = transferService.transfer(request);

        assertThat(response.transferId()).isNotNull();
        assertThat(response.status()).isEqualTo("TRANSFER_COMPLETED");

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("850.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("650.00");

        verify(transferRepository).save(any(Transfer.class));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(movementRepository).saveAll(anyList());
        verify(eventPublisher).publishEvent(any(TransferCompletedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account fromAccount = new Account(
                fromAccountId,
                "Origem",
                new BigDecimal("100.00")
        );

        Account toAccount = new Account(
                toAccountId,
                "Destino",
                new BigDecimal("500.00")
        );

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("150.00")
        );

        when(accountRepository.findAllByIdWithLock(anyList()))
                .thenReturn(List.of(fromAccount, toAccount));

        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.transfer(request);
        });

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("100.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("500.00");

        verify(transferRepository, never()).save(any(Transfer.class));
        verify(movementRepository, never()).saveAll(anyList());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        TransferRequest request = new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ZERO
        );

        assertThrows(InvalidTransferException.class, () -> {
            transferService.transfer(request);
        });

        verify(accountRepository, never()).findAllByIdWithLock(anyList());
        verify(transferRepository, never()).save(any());
        verify(movementRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        TransferRequest request = new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("-10.00")
        );

        assertThrows(InvalidTransferException.class, () -> {
            transferService.transfer(request);
        });

        verify(accountRepository, never()).findAllByIdWithLock(anyList());
        verify(transferRepository, never()).save(any());
        verify(movementRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenTransferIsToSameAccount() {
        UUID accountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                accountId,
                accountId,
                new BigDecimal("100.00")
        );

        assertThrows(InvalidTransferException.class, () -> {
            transferService.transfer(request);
        });

        verify(accountRepository, never()).findAllByIdWithLock(anyList());
        verify(transferRepository, never()).save(any());
        verify(movementRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("100.00")
        );

        when(accountRepository.findAllByIdWithLock(anyList()))
                .thenReturn(List.of());

        assertThrows(AccountNotFoundException.class, () -> {
            transferService.transfer(request);
        });

        verify(transferRepository, never()).save(any());
        verify(movementRepository, never()).saveAll(anyList());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRegisterDebitAndCreditMovements() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account fromAccount = new Account(
                fromAccountId,
                "Origem",
                new BigDecimal("1000.00")
        );

        Account toAccount = new Account(
                toAccountId,
                "Destino",
                new BigDecimal("500.00")
        );

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("150.00")
        );

        when(accountRepository.findAllByIdWithLock(anyList()))
                .thenReturn(List.of(fromAccount, toAccount));

        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transfer(request);

        ArgumentCaptor<List<Movement>> movementsCaptor = ArgumentCaptor.forClass(List.class);

        verify(movementRepository).saveAll(movementsCaptor.capture());

        List<Movement> movements = movementsCaptor.getValue();

        assertThat(movements).hasSize(2);

        assertThat(movements)
                .extracting(Movement::getType)
                .containsExactlyInAnyOrder(MovementType.DEBIT, MovementType.CREDIT);

        assertThat(movements)
                .extracting(Movement::getAmount)
                .allSatisfy(amount -> assertThat(amount).isEqualByComparingTo("150.00"));

        assertThat(movements)
                .extracting(Movement::getTransferId)
                .doesNotContainNull();
    }

    @Test
    void shouldPublishTransferCompletedEvent() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account fromAccount = new Account(
                fromAccountId,
                "Origem",
                new BigDecimal("1000.00")
        );

        Account toAccount = new Account(
                toAccountId,
                "Destino",
                new BigDecimal("500.00")
        );

        TransferRequest request = new TransferRequest(
                fromAccountId,
                toAccountId,
                new BigDecimal("150.00")
        );

        when(accountRepository.findAllByIdWithLock(anyList()))
                .thenReturn(List.of(fromAccount, toAccount));

        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transfer(request);

        ArgumentCaptor<TransferCompletedEvent> eventCaptor =
                ArgumentCaptor.forClass(TransferCompletedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        TransferCompletedEvent event = eventCaptor.getValue();

        assertThat(event.transferId()).isNotNull();
        assertThat(event.fromAccountId()).isEqualTo(fromAccountId);
        assertThat(event.toAccountId()).isEqualTo(toAccountId);
        assertThat(event.amount()).isEqualByComparingTo("150.00");
    }
}