package realize.db.dtj.domain;

import jakarta.persistence.*;
import lombok.Getter;
import realize.db.dtj.utils.enumerations.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID transferId;

    @Column(nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant processedAt;

    private Instant nextAttemptAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    protected NotificationOutbox() {
    }

    public NotificationOutbox(
            UUID id,
            UUID transferId,
            UUID accountId,
            String type,
            String payload
    ) {
        this.id = id;
        this.transferId = transferId;
        this.accountId = accountId;
        this.type = type;
        this.payload = payload;
        this.status = NotificationStatus.PENDING;
        this.attempts = 0;
        this.createdAt = Instant.now();
    }

    public static NotificationOutbox transferCompleted(
            UUID transferId,
            UUID accountId,
            String payload
    ) {
        return new NotificationOutbox(
                UUID.randomUUID(),
                transferId,
                accountId,
                "TRANSFER_COMPLETED",
                payload
        );
    }

    public void markAsProcessing() {
        this.status = NotificationStatus.PROCESSING;
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.processedAt = Instant.now();
        this.errorMessage = null;
        this.nextAttemptAt = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.attempts++;
        this.errorMessage = errorMessage;
        this.nextAttemptAt = Instant.now().plusSeconds(60);
    }

    public void markAsPendingRetry(String errorMessage) {
        this.status = NotificationStatus.PENDING;
        this.attempts++;
        this.errorMessage = errorMessage;
        this.nextAttemptAt = Instant.now().plusSeconds(60L * attempts);
    }

}