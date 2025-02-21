package codes.dimitri.mediminder.api.medication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateMedicationRequestDTO(
    @NotBlank(message = "Name is required")
    @Size(max = 128, message = "Name cannot contain more than {max} characters")
    String name,
    @NotNull(message = "Administration type is required")
    String administrationTypeId,
    @NotNull(message = "Dose type is required")
    String doseTypeId,
    @NotNull(message = "The amount of doses per package is required")
    @PositiveOrZero(message = "The amount of doses per package must be zero or positive")
    BigDecimal dosesPerPackage,
    @NotNull(message = "The color is required")
    Color color) {
}
