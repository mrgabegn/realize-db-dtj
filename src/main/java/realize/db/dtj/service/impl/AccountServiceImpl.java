package realize.db.dtj.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import realize.db.dtj.domain.Account;
import realize.db.dtj.domain.Movement;
import realize.db.dtj.dto.request.CreateAccountRequest;
import realize.db.dtj.dto.response.AccountResponse;
import realize.db.dtj.dto.response.MovementResponse;
import realize.db.dtj.repository.AccountRepository;
import realize.db.dtj.repository.MovementRepository;
import realize.db.dtj.service.AccountService;
import realize.db.dtj.utils.exceptions.AccountNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            MovementRepository movementRepository
    ) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account(
                UUID.randomUUID(),
                request.name(),
                request.initialBalance()
        );

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada"));

        return toAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> getMovementsByAccountId(UUID accountId) {
        boolean accountExists = accountRepository.existsById(accountId);

        if (!accountExists) {
            throw new AccountNotFoundException("Conta não encontrada");
        }

        return movementRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getBalance()
        );
    }

    private MovementResponse toMovementResponse(Movement movement) {
        return new MovementResponse(
                movement.getId(),
                movement.getAccountId(),
                movement.getType(),
                movement.getAmount(),
                movement.getTransferId(),
                movement.getCreatedAt()
        );
    }
}