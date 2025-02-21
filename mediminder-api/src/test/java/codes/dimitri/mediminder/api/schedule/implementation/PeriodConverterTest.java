package codes.dimitri.mediminder.api.schedule.implementation;

import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodConverterTest {
    private PeriodConverter converter = new PeriodConverter();

    @Test
    void convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(Period.ofDays(1))).isEqualTo("P1D");
    }

    @Test
    void convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("P1D")).isEqualTo(Period.ofDays(1));
    }
}