package realize.db.dtj.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import realize.db.dtj.dto.event.TransferCompletedEvent;
import realize.db.dtj.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    public void notifyTransferCompleted(TransferCompletedEvent event) {
        log.info(
                "Notificação enviada. Transferência={}, origem={}, destino={}, valor={}",
                event.transferId(),
                event.fromAccountId(),
                event.toAccountId(),
                event.amount()
        );
    }
}