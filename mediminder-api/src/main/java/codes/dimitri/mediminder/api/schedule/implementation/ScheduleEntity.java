package codes.dimitri.mediminder.api.schedule.implementation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
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
    private UUID userId;
    private UUID medicationId;
    @Embedded
    private SchedulePeriodEntity period;
    private Period interval;
    private LocalTime time;
    private String description;
    private BigDecimal dose;

    public ScheduleEntity(UUID userId, UUID medicationId, SchedulePeriodEntity period, Period interval, LocalTime time, String description, BigDecimal dose) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.medicationId = medicationId;
        this.period = period;
        this.interval = interval;
        this.time = time;
        this.description = description;
        this.dose = dose;
    }

    public boolean isHappeningAt(LocalDate date) {
        if (!period.isContaining(date)) return false;
        long daysSinceStart = ChronoUnit.DAYS.between(period.getStartingAt(), date);
        int intervalDays = interval.getDays();
        return daysSinceStart % intervalDays == 0;
    }

    public BigDecimal calculateTakenDosesInPeriod(LocalDate start, LocalDate endInclusive) {
        LocalDate actualStart = start.isBefore(period.getStartingAt()) ? period.getStartingAt() : start;
        LocalDate actualEnd = period.getEndingAtInclusive() == null || period.getEndingAtInclusive().isAfter(endInclusive) ? endInclusive : period.getEndingAtInclusive();
        long daysBetweenDates = ChronoUnit.DAYS.between(actualStart, actualEnd) + 1;
        int intervalDays = interval.getDays();
        int timesTaken = (int) Math.ceil(((double) daysBetweenDates) / intervalDays);
        return dose.multiply(new BigDecimal(timesTaken));
    }
}
