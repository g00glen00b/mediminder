package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.NotificationType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
@Sql("classpath:test-data/notification.sql")
class NotificationEntityRepositoryTest {
    @Autowired
    private NotificationEntityRepository repository;

    @ParameterizedTest
    @CsvSource({
        "329b4906-af8e-4dfc-8516-781b3d5bef72,CABINET_ENTRY_EXPIRED,6ea65093-ce6f-4869-857a-184e7281e0b0,true",
        "329b4906-af8e-4dfc-8516-781b3d5bef72,CABINET_ENTRY_EXPIRED,04e33817-6fb7-44bc-b250-04ffc015125e,false",
        "329b4906-af8e-4dfc-8516-781b3d5bef72,INTAKE_EVENT,6ea65093-ce6f-4869-857a-184e7281e0b0,false",
        "329b4906-af8e-4dfc-8516-781b3d5bef72,CABINET_ENTRY_ALMOST_EXPIRED,6ea65093-ce6f-4869-857a-184e7281e0b0,true",
        "7024a2ff-a3e7-4374-91de-975993089acb,CABINET_ENTRY_ALMOST_EXPIRED,6ea65093-ce6f-4869-857a-184e7281e0b0,false"
    })
    void existsByUserIdAndTypeAndInitiatorId(UUID userId, NotificationType type, UUID initiatorId, boolean exists) {
        boolean result = repository.existsByUserIdAndTypeAndInitiatorId(userId, type, initiatorId);
        assertThat(result).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "329b4906-af8e-4dfc-8516-781b3d5bef72,2",
        "7024a2ff-a3e7-4374-91de-975993089acb,1",
        "d96bba8f-6637-469f-915d-c74a66a044d0,0"
    })
    void findAllActiveByUserId(UUID userId, int expectedResults) {
        var pageRequest = PageRequest.of(0, 10);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userId, pageRequest);
        assertThat(result.getTotalElements()).isEqualTo(expectedResults);
    }

    @ParameterizedTest
    @CsvSource({
        "db1a169c-376d-4d77-8d3d-17962911d468,329b4906-af8e-4dfc-8516-781b3d5bef72,true",
        "db1a169c-376d-4d77-8d3d-17962911d468,7024a2ff-a3e7-4374-91de-975993089acb,false",
        "bf844a6a-7d75-44a6-ab06-67757304f124,329b4906-af8e-4dfc-8516-781b3d5bef72,false",
        "bf844a6a-7d75-44a6-ab06-67757304f124,7024a2ff-a3e7-4374-91de-975993089acb,true"
    })
    void findByIdAndUserId(UUID id, UUID userId, boolean exists) {
        Optional<NotificationEntity> result = repository.findByIdAndUserId(id, userId);
        assertThat(result.isPresent()).isEqualTo(exists);
    }

    @ParameterizedTest
    @CsvSource({
        "2024-06-01T09:59:59Z,4",
        "2024-06-01T10:00:00Z,3",
        "2024-06-02T10:00:00Z,2",
        "2024-06-03T10:00:00Z,1",
        "2024-06-04T10:00:00Z,0",
    })
    void deleteAllByDeleteAtBefore(Instant date, int remainingResults) {
        repository.deleteAllByDeleteAtBefore(date);
        long results = repository.count();
        assertThat(results).isEqualTo(remainingResults);
    }
}