package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.schedule.InvalidScheduleException;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SchedulePeriodEntity {
    private LocalDate startingAt;
    private LocalDate endingAtInclusive;

    public static SchedulePeriodEntity ofEndInclusive(@NotNull LocalDate startingAt, @NotNull LocalDate endingAtInclusive) {
        if (endingAtInclusive.isBefore(startingAt)) {
            throw new InvalidScheduleException("End date has to go after the start date when given");
        }
        return new SchedulePeriodEntity(startingAt, endingAtInclusive);
    }

    public static SchedulePeriodEntity ofUnboundedEnd(@NotNull LocalDate startingAt) {
        return new SchedulePeriodEntity(startingAt, null);
    }

    public static SchedulePeriodEntity of(@NotNull LocalDate startingAt, LocalDate endingAtInclusive) {
        if (endingAtInclusive == null) return ofUnboundedEnd(startingAt);
        else return ofEndInclusive(startingAt, endingAtInclusive);
    }

    public boolean isContaining(LocalDate date) {
        return !startingAt.isAfter(date) && (endingAtInclusive == null || !endingAtInclusive.isBefore(date));
    }
}
