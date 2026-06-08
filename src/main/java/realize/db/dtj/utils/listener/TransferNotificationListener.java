package realize.db.dtj.utils.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import realize.db.dtj.dto.event.TransferCompletedEvent;
import realize.db.dtj.service.NotificationService;

@Component
public class TransferNotificationListener {

    private final NotificationService notificationService;

    public TransferNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        notificationService.notifyTransferCompleted(event);
    }
}