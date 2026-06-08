package realize.db.dtj.dto.response;

import realize.db.dtj.utils.enumerations.MovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementResponse(
        UUID id,
        UUID accountId,
        MovementType type,
        BigDecimal amount,
        UUID transferId,
        Instant createdAt
) {
}