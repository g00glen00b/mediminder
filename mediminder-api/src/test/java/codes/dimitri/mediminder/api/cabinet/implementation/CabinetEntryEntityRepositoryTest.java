package codes.dimitri.mediminder.api.cabinet.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
@Sql("classpath:test-data/cabinet-entries.sql")
class CabinetEntryEntityRepositoryTest {
    @Autowired
    private CabinetEntryEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "eaf1d029-d072-4554-8734-914bc4d7cb07,4",
        "ed9e7a22-ebe1-4627-929d-e63f174cf6af,1",
        "efc75cfd-aa5e-4922-99f2-5da07d53e7b0,0"
    })
    void findAllByUserId(UUID userId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<CabinetEntryEntity> results = repository.findAllByUserId(userId, pageRequest);
        assertThat(results.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "76bef166-1628-42ed-bf7f-609551586a2f,eaf1d029-d072-4554-8734-914bc4d7cb07,true",
        "76bef166-1628-42ed-bf7f-609551586a2f,ed9e7a22-ebe1-4627-929d-e63f174cf6af,false",
        "b7cfa15e-1fe5-44b1-913b-98a7a0018d6c,eaf1d029-d072-4554-8734-914bc4d7cb07,false",
        "b7cfa15e-1fe5-44b1-913b-98a7a0018d6c,ed9e7a22-ebe1-4627-929d-e63f174cf6af,true"
    })
    void findByIdAndUserId(UUID id, UUID userId, boolean exists) {
        Optional<CabinetEntryEntity> result = repository.findByIdAndUserId(id, userId);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-06-30,4",
        "2024-06-29,3",
        "2024-06-28,2",
        "2024-06-27,0"
    })
    void findAllWithRemainingDosesWithExpiryDateBefore(LocalDate date, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<CabinetEntryEntity> result = repository.findAllWithRemainingDosesWithExpiryDateBefore(date, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "bdeb432c-c1d7-4482-ae55-19c2750b7796,10",
        "65729ae5-a7b9-40a0-8299-ba26a6f05745,60"
    })
    void sumRemainingDosesByMedicationId(UUID medicationId, BigDecimal expectedDoses) {
        BigDecimal result = repository.sumRemainingDosesByMedicationId(medicationId);
        assertThat(result).isEqualByComparingTo(expectedDoses);
    }

    @ParameterizedTest
    @CsvSource({
        "bdeb432c-c1d7-4482-ae55-19c2750b7796,3",
        "ec544543-9aff-4172-989d-ebd5d08a0dea,4",
        "7ad0fd96-5635-47b9-8dd7-9397dad23988,5"
    })
    void deleteAllByMedicationId(UUID medicationId, int expectedRemainingResults) {
        repository.deleteAllByMedicationId(medicationId);
        assertThat(repository.count()).isEqualTo(expectedRemainingResults);
    }

    @ParameterizedTest
    @CsvSource({
        "bdeb432c-c1d7-4482-ae55-19c2750b7796,1",
        "65729ae5-a7b9-40a0-8299-ba26a6f05745,2",
        "ec544543-9aff-4172-989d-ebd5d08a0dea,1",
        "7ad0fd96-5635-47b9-8dd7-9397dad23988,0"
    })
    void findAllWithRemainingDosesByMedicationId(UUID medicationId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<CabinetEntryEntity> results = repository.findAllWithRemainingDosesByMedicationId(medicationId, pageRequest);
        assertThat(results.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "bdeb432c-c1d7-4482-ae55-19c2750b7796,2",
        "65729ae5-a7b9-40a0-8299-ba26a6f05745,2",
        "ec544543-9aff-4172-989d-ebd5d08a0dea,1",
        "7ad0fd96-5635-47b9-8dd7-9397dad23988,0"
    })
    void findAllByMedicationId(UUID medicationId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
        assertThat(results.getTotalElements()).isEqualTo(expectedResults);
    }
}