package realize.db.dtj.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import realize.db.dtj.domain.Account;
import realize.db.dtj.domain.Movement;
import realize.db.dtj.domain.Transfer;
import realize.db.dtj.dto.event.TransferCompletedEvent;
import realize.db.dtj.dto.request.TransferRequest;
import realize.db.dtj.dto.response.TransferResponse;
import realize.db.dtj.repository.AccountRepository;
import realize.db.dtj.repository.MovementRepository;
import realize.db.dtj.repository.TransferRepository;
import realize.db.dtj.service.TransferService;
import realize.db.dtj.utils.enumerations.MovementType;
import realize.db.dtj.utils.exceptions.AccountNotFoundException;
import realize.db.dtj.utils.exceptions.InvalidTransferException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final TransferRepository transferRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransferServiceImpl(
            AccountRepository accountRepository,
            MovementRepository movementRepository,
            TransferRepository transferRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.transferRepository = transferRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        validateRequest(request);

        List<UUID> ids = Stream.of(request.fromAccountId(), request.toAccountId())
                .sorted()
                .toList();

        List<Account> lockedAccounts = accountRepository.findAllByIdWithLock(ids);

        if (lockedAccounts.size() != 2) {
            throw new AccountNotFoundException("Conta de origem ou destino não encontrada");
        }

        Map<UUID, Account> accountsById = lockedAccounts.stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        Account from = accountsById.get(request.fromAccountId());
        Account to = accountsById.get(request.toAccountId());

        if (from == null || to == null) {
            throw new AccountNotFoundException("Conta de origem ou destino não encontrada");
        }

        BigDecimal amount = request.amount();

        Transfer transfer = Transfer.create(
                from.getId(),
                to.getId(),
                amount
        );

        from.debit(amount);
        to.credit(amount);

        accountRepository.save(from);
        accountRepository.save(to);

        transfer.markAsCompleted();
        transferRepository.save(transfer);

        UUID transferId = transfer.getId();
        Instant now = Instant.now();

        Movement debit = new Movement(
                UUID.randomUUID(),
                from.getId(),
                MovementType.DEBIT,
                amount,
                transferId,
                now
        );

        Movement credit = new Movement(
                UUID.randomUUID(),
                to.getId(),
                MovementType.CREDIT,
                amount,
                transferId,
                now
        );

        movementRepository.saveAll(List.of(debit, credit));

        eventPublisher.publishEvent(new TransferCompletedEvent(
                transferId,
                from.getId(),
                to.getId(),
                amount
        ));

        return new TransferResponse(transferId, "TRANSFER_COMPLETED");
    }

    private void validateRequest(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidTransferException("Conta de origem e destino não podem ser iguais");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Valor da transferência deve ser maior que zero");
        }
    }
}