package codes.dimitri.mediminder.api.user.implementation;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ZoneIdConverterTest {
    private ZoneIdConverter converter = new ZoneIdConverter();

    @Test
    void convertToDatabaseColumn() {
        // Given
        var zoneId = ZoneId.of("Europe/Brussels");
        // Then
        assertThat(converter.convertToDatabaseColumn(zoneId)).isEqualTo("Europe/Brussels");
    }

    @Test
    void convertToEntityAttribute() {
        // Given
        var zoneId = "Europe/Brussels";
        // Then
        assertThat(converter.convertToEntityAttribute(zoneId)).isEqualTo(ZoneId.of("Europe/Brussels"));
    }
}