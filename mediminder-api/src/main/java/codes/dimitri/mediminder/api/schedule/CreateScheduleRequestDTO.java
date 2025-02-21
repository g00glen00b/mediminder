package codes.dimitri.mediminder.api.schedule;

import codes.dimitri.mediminder.api.schedule.implementation.PositiveInterval;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.Period;
import java.util.UUID;

public record CreateScheduleRequestDTO(
    @NotNull(message = "Medication is required")
    UUID medicationId,
    @NotNull(message = "Interval is required")
    @PositiveInterval(message = "Interval must be positive")
    Period interval,
    @NotNull(message = "Period is required")
    @Valid
    SchedulePeriodDTO period,
    @NotNull(message = "Time is required")
    LocalTime time,
    @Size(max = 128, message = "Description should not contain more than {max} characters")
    String description,
    @NotNull(message = "Dose is required")
    @Positive(message = "Dose should be positive")
    BigDecimal dose
) {
}
