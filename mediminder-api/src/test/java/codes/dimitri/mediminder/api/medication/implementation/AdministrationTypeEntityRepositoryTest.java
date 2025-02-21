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
class AdministrationTypeEntityRepositoryTest {
    @Autowired
    private AdministrationTypeEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "SYRUP,1",
        "TABLET,3",
        "INJECTION,6",
        "DOESNOTEXIST,0"
    })
    void findAllByMedicationTypeId(String medicationTypeId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<AdministrationTypeEntity> results = repository.findAllByMedicationTypeId(medicationTypeId, pageRequest);
        assertThat(results.getContent()).hasSize(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "ORAL,SYRUP,true",
        "ORAL,INJECTION,false",
        "DOESNOTEXIST,INJECTION,false",
        "ORAL,DOESNOTEXIST,false"
    })
    void findByIdAndMedicationTypeId(String id, String medicationTypeId, boolean exists) {
        Optional<AdministrationTypeEntity> result = repository.findByIdAndMedicationTypeId(id, medicationTypeId);
        assertThat(result.isPresent()).isEqualTo(exists);
    }
}