package realize.db.dtj.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID transferId,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount
) {
}