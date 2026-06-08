package realize.db.dtj.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import realize.db.dtj.utils.enumerations.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID fromAccountId;

    @Column(nullable = false)
    private UUID toAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    protected Transfer() {
    }

    public Transfer(
            UUID id,
            UUID fromAccountId,
            UUID toAccountId,
            BigDecimal amount
    ) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = TransferStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static Transfer create(
            UUID fromAccountId,
            UUID toAccountId,
            BigDecimal amount
    ) {
        return new Transfer(
                UUID.randomUUID(),
                fromAccountId,
                toAccountId,
                amount
        );
    }

    public void markAsCompleted() {
        this.status = TransferStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markAsFailed() {
        this.status = TransferStatus.FAILED;
    }

}