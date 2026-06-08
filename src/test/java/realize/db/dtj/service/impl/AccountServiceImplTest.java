package realize.db.dtj.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import realize.db.dtj.domain.Account;
import realize.db.dtj.domain.Movement;
import realize.db.dtj.dto.request.CreateAccountRequest;
import realize.db.dtj.dto.response.AccountResponse;
import realize.db.dtj.dto.response.MovementResponse;
import realize.db.dtj.repository.AccountRepository;
import realize.db.dtj.repository.MovementRepository;
import realize.db.dtj.utils.enumerations.MovementType;
import realize.db.dtj.utils.exceptions.AccountNotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldCreateAccountSuccessfully() {
        CreateAccountRequest request = new CreateAccountRequest(
                "Gabriel Nascimento",
                new BigDecimal("1000.00")
        );

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Gabriel Nascimento");
        assertThat(response.balance()).isEqualByComparingTo("1000.00");

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldGetAccountByIdSuccessfully() {
        UUID accountId = UUID.randomUUID();

        Account account = new Account(
                accountId,
                "Gabriel Nascimento",
                new BigDecimal("1000.00")
        );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccountById(accountId);

        assertThat(response.id()).isEqualTo(accountId);
        assertThat(response.name()).isEqualTo("Gabriel Nascimento");
        assertThat(response.balance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccountById(accountId);
        });
    }

    @Test
    void shouldGetMovementsByAccountIdSuccessfully() {
        UUID accountId = UUID.randomUUID();
        UUID transferId = UUID.randomUUID();

        Movement movement = new Movement(
                UUID.randomUUID(),
                accountId,
                MovementType.DEBIT,
                new BigDecimal("150.00"),
                transferId,
                Instant.now()
        );

        when(accountRepository.existsById(accountId))
                .thenReturn(true);

        when(movementRepository.findByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(movement));

        List<MovementResponse> response = accountService.getMovementsByAccountId(accountId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().accountId()).isEqualTo(accountId);
        assertThat(response.getFirst().type()).isEqualTo(MovementType.DEBIT);
        assertThat(response.getFirst().amount()).isEqualByComparingTo("150.00");
        assertThat(response.getFirst().transferId()).isEqualTo(transferId);
    }
}