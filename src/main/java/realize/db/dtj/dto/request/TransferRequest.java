package realize.db.dtj.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(

        @NotNull(message = "Conta de origem é obrigatória")
        UUID fromAccountId,

        @NotNull(message = "Conta de destino é obrigatória")
        UUID toAccountId,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor da transferência deve ser maior que zero")
        BigDecimal amount
) {
}