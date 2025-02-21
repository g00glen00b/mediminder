package codes.dimitri.mediminder.api.medication.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
class DoseTypeEntityRepositoryTest {
    @Autowired
    private DoseTypeEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "TABLET,1",
        "INJECTION,3",
        "DOESNOTEXIST,0"
    })
    void findAllByMedicationTypeId(String medicationTypeId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<DoseTypeEntity> results = repository.findAllByMedicationTypeId(medicationTypeId, pageRequest);
        assertThat(results.getContent()).hasSize(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "MILLILITER,SYRUP,true",
        "MILLILITER,TABLET,false",
        "DOESNOTEXIST,TABLET,false",
        "MILLILITER,DOESNOTEXIST,false"
    })
    void findByIdAndMedicationTypeId(String id, String medicationTypeId, boolean exists) {
        Optional<DoseTypeEntity> result = repository.findByIdAndMedicationTypeId(id, medicationTypeId);
        assertThat(result.isPresent()).isEqualTo(exists);
    }
}