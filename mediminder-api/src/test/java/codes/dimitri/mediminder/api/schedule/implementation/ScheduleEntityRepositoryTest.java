package codes.dimitri.mediminder.api.schedule.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
@Sql("classpath:test-data/schedule.sql")
class ScheduleEntityRepositoryTest {
    @Autowired
    private ScheduleEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "9133c9d2-0b6c-4915-9752-512d2dca9330,3",
        "b47e0b6f-be52-4e38-8301-fe60d08cbfbe,1",
        "ae0fa147-6e23-4aff-a13a-12f674e686a0,0"
    })
    void findAllByUserId(UUID userId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<ScheduleEntity> result = repository.findAllByUserId(userId, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "6ba61df2-ab46-4909-b9e6-233ea47dd701,9133c9d2-0b6c-4915-9752-512d2dca9330,true",
        "6ba61df2-ab46-4909-b9e6-233ea47dd701,b47e0b6f-be52-4e38-8301-fe60d08cbfbe,false",
        "945b1bea-b447-4701-a137-3e447c35ffa3,9133c9d2-0b6c-4915-9752-512d2dca9330,false",
        "945b1bea-b447-4701-a137-3e447c35ffa3,b47e0b6f-be52-4e38-8301-fe60d08cbfbe,true",
    })
    void findByIdAndUserId(UUID id, UUID userId, boolean exists) {
        Optional<ScheduleEntity> result = repository.findByIdAndUserId(id, userId);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "9133c9d2-0b6c-4915-9752-512d2dca9330,2024-05-30,0",
        "9133c9d2-0b6c-4915-9752-512d2dca9330,2024-05-31,1",
        "9133c9d2-0b6c-4915-9752-512d2dca9330,2024-06-29,1",
        "9133c9d2-0b6c-4915-9752-512d2dca9330,2024-06-30,3",
        "9133c9d2-0b6c-4915-9752-512d2dca9330,2024-07-31,3",
        "9133c9d2-0b6c-4915-9752-512d2dca9330,2024-08-01,1",
        "b47e0b6f-be52-4e38-8301-fe60d08cbfbe,2024-06-30,1"
    })
    void findAllByUserIdWithDateInPeriod(UUID userId, LocalDate date, int expectedResults) {
        List<ScheduleEntity> results = repository.findAllByUserIdWithDateInPeriod(userId, date);
        assertThat(results).hasSize(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-05-30,0",
        "2024-05-31,1",
        "2024-06-29,1",
        "2024-06-30,3",
        "2024-07-31,3",
        "2024-08-01,2"
    })
    void findAllWithUserScheduledMedicationOnDate(LocalDate date, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<UserScheduledMedication> result = repository.findAllWithUserScheduledMedicationOnDate(date, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "0b845403-3b16-436f-b84a-925b01421ad9,2024-04-30,2024-05-30,0",
        "0b845403-3b16-436f-b84a-925b01421ad9,2024-04-30,2024-05-31,1",
        "0b845403-3b16-436f-b84a-925b01421ad9,2024-04-30,2024-06-30,2",
        "0b845403-3b16-436f-b84a-925b01421ad9,2024-05-31,2024-07-31,2",
        "0b845403-3b16-436f-b84a-925b01421ad9,2024-08-01,2024-08-31,0",
        "a9356fca-da82-48ab-af04-a7169b91ea4f,2024-04-30,2024-06-29,0",
        "a9356fca-da82-48ab-af04-a7169b91ea4f,2024-04-30,2024-06-30,1",
        "a9356fca-da82-48ab-af04-a7169b91ea4f,2024-06-30,2024-07-31,1"
    })
    void findAllByMedicationIdAndDateInPeriodGroup(UUID medicationId, LocalDate startingAt, LocalDate endingAtInclusive, int expectedResults) {
        List<ScheduleEntity> result = repository.findAllByMedicationIdAndDateInPeriodGroup(startingAt, endingAtInclusive, medicationId);
        assertThat(result).hasSize(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "0b845403-3b16-436f-b84a-925b01421ad9,2",
        "a9356fca-da82-48ab-af04-a7169b91ea4f,3",
        "fb384363-0446-4fdc-a62d-098c20ddf286,3",
        "4dfe0cfe-5f78-4a62-8956-dd61fcc9737d,4"
    })
    void deleteAllByMedicationId(UUID medicationId, int expectedResults) {
        repository.deleteAllByMedicationId(medicationId);
        assertThat(repository.count()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-04-30,2024-05-30,0",
        "2024-04-30,2024-05-31,1",
        "2024-04-30,2024-06-30,4",
        "2024-05-31,2024-07-31,4",
        "2024-08-01,2024-08-31,2",
    })
    void findAllByOverlappingPeriod(LocalDate startingAt, LocalDate endingAtInclusive, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<ScheduleEntity> results = repository.findAllByOverlappingPeriod(startingAt, endingAtInclusive, pageRequest);
        assertThat(results.getTotalElements()).isEqualTo(expectedResults);
    }
}