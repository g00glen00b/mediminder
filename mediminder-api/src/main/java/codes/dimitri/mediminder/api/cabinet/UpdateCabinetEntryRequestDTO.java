package codes.dimitri.mediminder.api.cabinet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCabinetEntryRequestDTO(
    @NotNull(message = "The amount of remaining doses is required")
    @PositiveOrZero(message = "The amount of remaining doses must be zero or positive")
    BigDecimal remainingDoses,
    @NotNull(message = "Expiry date is required")
    LocalDate expiryDate
) {
}
