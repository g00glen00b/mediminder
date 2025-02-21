package codes.dimitri.mediminder.api.medication.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
@Sql("classpath:test-data/medication.sql")
class MedicationEntityRepositoryTest {
    @Autowired
    private MedicationEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "3257ee2d-b6c6-4a12-990e-826a80c43f17,2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,true",
        "3257ee2d-b6c6-4a12-990e-826a80c43f17,cc3211ec-e3a9-4a6d-a406-1c8f31ef203b,false",
        "4579fa76-1edc-4113-b521-2167713a3636,2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,false",
        "4579fa76-1edc-4113-b521-2167713a3636,cc3211ec-e3a9-4a6d-a406-1c8f31ef203b,true",
    })
    void findByIdAndUserId(UUID id, UUID userId, boolean exists) {
        Optional<MedicationEntity> result = repository.findByIdAndUserId(id, userId);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,3",
        "cc3211ec-e3a9-4a6d-a406-1c8f31ef203b,2",
        "d4b57f56-46da-4c64-b349-b25a22145365,0"
    })
    void findAllByUserId(UUID userId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<MedicationEntity> result = repository.findAllByUserId(userId, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,daf,2",
        "2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,ibu,1",
        "2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,zaf,0",
        "2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124,bla,0"
    })
    void findAllByUserIdAndNameContainingIgnoreCase(UUID userId, String search, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<MedicationEntity> result = repository.findAllByUserIdAndNameContainingIgnoreCase(userId, search, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }
}