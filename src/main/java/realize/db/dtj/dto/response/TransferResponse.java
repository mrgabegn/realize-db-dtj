package realize.db.dtj.dto.response;

import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        String status
) {
}