package codes.dimitri.mediminder.api.medication;

import java.math.BigDecimal;
import java.util.UUID;

public record MedicationDTO(
    UUID id,
    String name,
    MedicationTypeDTO medicationType,
    AdministrationTypeDTO administrationType,
    DoseTypeDTO doseType,
    BigDecimal dosesPerPackage,
    Color color
) {
}
