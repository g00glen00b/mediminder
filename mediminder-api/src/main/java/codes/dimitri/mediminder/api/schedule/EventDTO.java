package codes.dimitri.mediminder.api.schedule;

import codes.dimitri.mediminder.api.medication.MedicationDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventDTO(
    UUID id,
    UUID scheduleId,
    MedicationDTO medication,
    LocalDateTime targetDate,
    LocalDateTime completedDate,
    BigDecimal dose,
    String description
) {
}
