package codes.dimitri.mediminder.api.schedule.implementation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntity {
    @Id
    private UUID id;
    private String userId;
    private UUID medicationId;
    @Embedded
    private SchedulePeriodEntity period;
    private Period interval;
    private LocalTime time;
    private String description;
    private BigDecimal dose;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "schedule")
    private List<CompletedEventEntity> completedEvents;

    public ScheduleEntity(String userId, UUID medicationId, SchedulePeriodEntity period, Period interval, LocalTime time, String description, BigDecimal dose) {
        this(UUID.randomUUID(), userId, medicationId, period, interval, time, description, dose);
    }

    public ScheduleEntity(UUID id, String userId, UUID medicationId, SchedulePeriodEntity period, Period interval, LocalTime time, String description, BigDecimal dose) {
        this(id, userId, medicationId, period, interval, time, description, dose, new ArrayList<>());
    }

    public boolean isHappeningAt(LocalDate date) {
        if (!period.isContaining(date)) return false;
        long daysSinceStart = ChronoUnit.DAYS.between(period.getStartingAt(), date);
        int intervalDays = interval.getDays();
        return daysSinceStart % intervalDays == 0;
    }

    public BigDecimal calculateTakenDosesInPeriod(LocalDate start, LocalDate endInclusive) {
        int intervalDays = interval.getDays();
        LocalDate actualEnd = period.getEndingAtInclusive() == null || period.getEndingAtInclusive().isAfter(endInclusive) ? endInclusive : period.getEndingAtInclusive();
        LocalDate actualStart = start.isAfter(period.getStartingAt()) ?  start : period.getStartingAt();
        long daysBetweenStartEndGivenEnd = ChronoUnit.DAYS.between(period.getStartingAt(), actualEnd) + 1;
        long daysBetweenStartAndGivenStart = ChronoUnit.DAYS.between(period.getStartingAt(), actualStart);
        int timesTakenTotal = (int) Math.ceil(((double) daysBetweenStartEndGivenEnd) / intervalDays);
        int timesTakenBefore = (int) Math.ceil(((double) daysBetweenStartAndGivenStart) / intervalDays);
        return dose.multiply(new BigDecimal(timesTakenTotal - timesTakenBefore));
    }
}
