package codes.dimitri.mediminder.api.schedule;

import codes.dimitri.mediminder.api.medication.MedicationDTO;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.Period;
import java.util.UUID;

public record ScheduleDTO(
    UUID id,
    String userId,
    MedicationDTO medication,
    Period interval,
    SchedulePeriodDTO period,
    String description,
    BigDecimal dose,
    LocalTime time
) {
}
