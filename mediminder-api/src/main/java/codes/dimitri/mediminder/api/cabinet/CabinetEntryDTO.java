package codes.dimitri.mediminder.api.cabinet;

import codes.dimitri.mediminder.api.medication.MedicationDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CabinetEntryDTO(
    UUID id,
    String userId,
    MedicationDTO medication,
    BigDecimal remainingDoses,
    LocalDate expiryDate
) {
}
