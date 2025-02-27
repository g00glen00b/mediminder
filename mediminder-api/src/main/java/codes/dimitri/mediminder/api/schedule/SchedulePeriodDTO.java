package codes.dimitri.mediminder.api.schedule;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@PositiveSchedulePeriod(message = "End date must be after the starting date")
public record SchedulePeriodDTO(
    @NotNull(message = "The starting date is required")
    LocalDate startingAt,
    LocalDate endingAtInclusive) {
}
