package realize.db.dtj.service;

import realize.db.dtj.dto.event.TransferCompletedEvent;

public interface NotificationService {
    void notifyTransferCompleted(TransferCompletedEvent event);
}
