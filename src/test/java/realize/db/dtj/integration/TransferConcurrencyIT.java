package realize.db.dtj.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import realize.db.dtj.domain.Account;
import realize.db.dtj.dto.request.TransferRequest;
import realize.db.dtj.repository.AccountRepository;
import realize.db.dtj.repository.MovementRepository;
import realize.db.dtj.repository.TransferRepository;
import realize.db.dtj.service.impl.TransferServiceImpl;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransferConcurrencyIT {

    @Autowired
    private TransferServiceImpl transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private TransferRepository transferRepository;

    private UUID fromAccountId;
    private UUID toAccountId;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll();
        transferRepository.deleteAll();
        accountRepository.deleteAll();

        fromAccountId = UUID.randomUUID();
        toAccountId = UUID.randomUUID();

        Account fromAccount = new Account(
                fromAccountId,
                "Conta Origem",
                new BigDecimal("1000.00")
        );

        Account toAccount = new Account(
                toAccountId,
                "Conta Destino",
                BigDecimal.ZERO
        );

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Test
    void shouldKeepBalancesConsistentWhenTransfersAreConcurrent() throws InterruptedException {
        int numberOfTransfers = 20;
        BigDecimal transferAmount = new BigDecimal("10.00");

        var executor = Executors.newFixedThreadPool(10);

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(numberOfTransfers);

        for (int i = 0; i < numberOfTransfers; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();

                    TransferRequest request = new TransferRequest(
                            fromAccountId,
                            toAccountId,
                            transferAmount
                    );

                    transferService.transfer(request);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();

        boolean finished = endGate.await(20, TimeUnit.SECONDS);

        executor.shutdown();

        assertThat(finished).isTrue();

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow();

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow();

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("800.00");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("200.00");

        BigDecimal totalBalance = fromAccount.getBalance().add(toAccount.getBalance());

        assertThat(totalBalance).isEqualByComparingTo("1000.00");

        assertThat(movementRepository.findByAccountIdOrderByCreatedAtDesc(fromAccountId))
                .hasSize(20);

        assertThat(movementRepository.findByAccountIdOrderByCreatedAtDesc(toAccountId))
                .hasSize(20);
    }
}