package codes.dimitri.mediminder.api.schedule;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SchedulePeriodDTO(
    @NotNull(message = "The starting date is required")
    LocalDate startingAt,
    LocalDate endingAtInclusive) {
}
