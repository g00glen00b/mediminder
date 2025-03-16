package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDeletedEvent;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.schedule.EventCompletedEvent;
import codes.dimitri.mediminder.api.schedule.EventUncompletedEvent;
import codes.dimitri.mediminder.api.user.UserManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
@Sql("classpath:test-data/cabinet-entries.sql")
@Sql(value = "classpath:test-data/cleanup-cabinet-entries.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CabinetEntryEventListenerTest {
    @Autowired
    private CabinetEntryEventListener listener;
    @Autowired
    private CabinetEntryEntityRepository repository;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private MedicationManager medicationManager;

    @Nested
    class handleMedicationDeletedEvent {
        @Test
        void deletesEntries() {
            assertThat(repository.count()).isEqualTo(5);
            var event = new MedicationDeletedEvent(UUID.fromString("bdeb432c-c1d7-4482-ae55-19c2750b7796"));
            listener.handleMedicationDeletedEvent(event);
            assertThat(repository.findAll())
                .hasSize(3)
                .extracting(CabinetEntryEntity::getId)
                .doesNotContain(
                    UUID.fromString("76bef166-1628-42ed-bf7f-609551586a2f"),
                    UUID.fromString("b993e814-394b-438c-b42e-4b97fa4d8739"));
        }
    }

    @Nested
    class handleEventCompletedEvent {
        @Test
        void subtractsDoses() {
            var event = new EventCompletedEvent(
                UUID.randomUUID(),
                UUID.fromString("eaf1d029-d072-4554-8734-914bc4d7cb07"),
                UUID.randomUUID(),
                UUID.fromString("bdeb432c-c1d7-4482-ae55-19c2750b7796"),
                LocalDateTime.of(2025, 3, 10, 10, 0, 0),
                LocalDateTime.of(2025, 3, 10, 10, 2, 0),
                BigDecimal.ONE
            );
            listener.handleEventCompletedEvent(event);
            assertThat(repository
                .findById(UUID.fromString("b993e814-394b-438c-b42e-4b97fa4d8739"))
                .orElseThrow())
                .extracting(CabinetEntryEntity::getRemainingDoses)
                .isEqualTo(new BigDecimal("9"));
        }
    }

    @Nested
    class handleEventUncompleted {
        @Test
        void addsDoses() {
            var event = new EventUncompletedEvent(
                UUID.randomUUID(),
                UUID.fromString("eaf1d029-d072-4554-8734-914bc4d7cb07"),
                UUID.randomUUID(),
                UUID.fromString("bdeb432c-c1d7-4482-ae55-19c2750b7796"),
                LocalDateTime.of(2025, 3, 10, 10, 0, 0),
                LocalDateTime.of(2025, 3, 10, 10, 2, 0),
                BigDecimal.ONE
            );
            listener.handleEventUncompletedEvent(event);
            assertThat(repository
                .findById(UUID.fromString("b993e814-394b-438c-b42e-4b97fa4d8739"))
                .orElseThrow())
                .extracting(CabinetEntryEntity::getRemainingDoses)
                .isEqualTo(new BigDecimal("11"));
        }
    }
}