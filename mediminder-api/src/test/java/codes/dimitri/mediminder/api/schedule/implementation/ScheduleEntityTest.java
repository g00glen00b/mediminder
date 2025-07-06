package codes.dimitri.mediminder.api.schedule.implementation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleEntityTest {
    @Nested
    class calculateTakenDosesInPeriod {
        @ParameterizedTest
        @CsvSource({
            "2025-07-02,2025-07-09,0",
            "2025-06-25,2025-07-09,0",
            "2025-06-24,2025-07-09,1",
            "2025-01-01,2025-07-09,1",
            "2025-06-24,2025-08-04,1",
            "2025-06-24,2025-08-05,2"
        })
        void returnsResult(LocalDate start, LocalDate endInclusive, BigDecimal expected) {
            ScheduleEntity entity = new ScheduleEntity(
                "auth|123",
                UUID.randomUUID(),
                SchedulePeriodEntity.of(LocalDate.of(2025, 6, 24), null),
                Period.ofWeeks(6),
                LocalTime.of(20, 0),
                null,
                BigDecimal.ONE
            );
            BigDecimal result = entity.calculateTakenDosesInPeriod(start, endInclusive);
            assertThat(result).isEqualByComparingTo(expected);
        }
    }
}