package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
@Sql({"classpath:test-data/schedules.sql", "classpath:test-data/completed-events.sql"})
@Sql(value = {"classpath:test-data/cleanup-completed-events.sql", "classpath:test-data/cleanup-schedules.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RecordApplicationEvents
class EventManagerImplTest {
    @Autowired
    private EventManager eventManager;
    @Autowired
    private CompletedEventEntityRepository repository;
    @Autowired
    private ScheduleEntityRepository scheduleRepository;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private MedicationManager medicationManager;
    @Autowired
    private ApplicationEvents events;

    @Nested
    class findAll {
        @Test
        void returnsResults() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication1 = new MedicationDTO(
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("1"),
                Color.RED
            );
            var medication2 = new MedicationDTO(
                UUID.fromString("a9356fca-da82-48ab-af04-a7169b91ea4f"),
                "Hydrocortisone 8mg",
                new MedicationTypeDTO("CAPSULE", "Capsule"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("CAPSULE", "capsule(s)"),
                new BigDecimal("1"),
                Color.YELLOW
            );
            when(medicationManager.findByIdForCurrentUser(medication1.id())).thenReturn(medication1);
            when(medicationManager.findByIdForCurrentUser(medication2.id())).thenReturn(medication2);
            when(userManager.findCurrentUser()).thenReturn(user);
            var events = eventManager.findAll(LocalDate.of(2024, 6, 30));
            assertThat(events).containsExactly(
                new EventDTO(
                    UUID.fromString("ebb5c232-2f2c-4c08-a2b6-d5ccc81ac08d"),
                    UUID.fromString("6ba61df2-ab46-4909-b9e6-233ea47dd701"),
                    medication1,
                    LocalDateTime.of(2024, 6, 30, 10, 0),
                    LocalDateTime.of(2024, 6, 30, 10, 1),
                    new BigDecimal("1"),
                    "Before breakfast"
                ),
                new EventDTO(
                    UUID.fromString("23366793-fe7d-4ea7-af3b-5c8b1352c5f2"),
                    UUID.fromString("08a6aa16-8449-418e-93ff-c7975731066d"),
                    medication2,
                    LocalDateTime.of(2024, 6, 30, 10, 0),
                    LocalDateTime.of(2024, 6, 30, 10, 2),
                    new BigDecimal("1"),
                    "Before breakfast"
                ),
                new EventDTO(
                    null,
                    UUID.fromString("f2f2de45-3000-45fc-af12-fa8cfce5c2ff"),
                    medication1,
                    LocalDateTime.of(2024, 6, 30, 18, 0),
                    null,
                    new BigDecimal("1"),
                    "After dinner"
                )
            );
        }

        @Test
        void returnsEmptyMedicationIfNotFound() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication1 = new MedicationDTO(
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("1"),
                Color.RED
            );
            when(medicationManager.findByIdForCurrentUser(medication1.id())).thenReturn(medication1);
            when(userManager.findCurrentUser()).thenReturn(user);
            var events = eventManager.findAll(LocalDate.of(2024, 6, 30));
            assertThat(events).containsExactly(
                new EventDTO(
                    UUID.fromString("ebb5c232-2f2c-4c08-a2b6-d5ccc81ac08d"),
                    UUID.fromString("6ba61df2-ab46-4909-b9e6-233ea47dd701"),
                    medication1,
                    LocalDateTime.of(2024, 6, 30, 10, 0),
                    LocalDateTime.of(2024, 6, 30, 10, 1),
                    new BigDecimal("1"),
                    "Before breakfast"
                ),
                new EventDTO(
                    UUID.fromString("23366793-fe7d-4ea7-af3b-5c8b1352c5f2"),
                    UUID.fromString("08a6aa16-8449-418e-93ff-c7975731066d"),
                    null,
                    LocalDateTime.of(2024, 6, 30, 10, 0),
                    LocalDateTime.of(2024, 6, 30, 10, 2),
                    new BigDecimal("1"),
                    "Before breakfast"
                ),
                new EventDTO(
                    null,
                    UUID.fromString("f2f2de45-3000-45fc-af12-fa8cfce5c2ff"),
                    medication1,
                    LocalDateTime.of(2024, 6, 30, 18, 0),
                    null,
                    new BigDecimal("1"),
                    "After dinner"
                )
            );
        }

        @Test
        void failsIfUserNotAuthenticated() {
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> eventManager.findAll(LocalDate.of(2024, 6, 30)))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfNoTargetDateGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> eventManager.findAll(null));
        }
    }

    @Nested
    class complete {
        @Test
        void returnsResult() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("1"),
                Color.RED
            );
            var currentTimeForUser = LocalDateTime.of(2024, 7, 3, 10, 1);
            UUID scheduleId = UUID.fromString("6ba61df2-ab46-4909-b9e6-233ea47dd701");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            when(userManager.calculateTodayForUser(user.id())).thenReturn(currentTimeForUser);
            var event = eventManager.complete(scheduleId, LocalDate.of(2024, 7, 3));
            assertThat(event).isEqualTo(
                new EventDTO(
                    event.id(),
                    scheduleId,
                    medication,
                    LocalDateTime.of(2024, 7, 3, 10, 0),
                    currentTimeForUser,
                    new BigDecimal("1"),
                    "Before breakfast"
                )
            );
        }

        @Test
        void savesEntity() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("1"),
                Color.RED
            );
            var currentTimeForUser = LocalDateTime.of(2024, 7, 3, 10, 1);
            UUID scheduleId = UUID.fromString("6ba61df2-ab46-4909-b9e6-233ea47dd701");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            when(userManager.calculateTodayForUser(user.id())).thenReturn(currentTimeForUser);
            EventDTO result = eventManager.complete(scheduleId, LocalDate.of(2024, 7, 3));
            CompletedEventEntity entity = repository.findById(result.id()).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new CompletedEventEntity(
                    result.id(),
                    user.id(),
                    scheduleRepository.findById(scheduleId).orElseThrow(),
                    LocalDateTime.of(2024, 7, 3, 10, 0),
                    LocalDateTime.of(2024, 7, 3, 10, 1),
                    new BigDecimal("1")
                ));
        }

        @Test
        void failsIfAlreadyCompleted() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("1"),
                Color.RED
            );
            var currentTimeForUser = LocalDateTime.of(2024, 6, 30, 10, 1);
            UUID scheduleId = UUID.fromString("6ba61df2-ab46-4909-b9e6-233ea47dd701");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            when(userManager.calculateTodayForUser(user.id())).thenReturn(currentTimeForUser);
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> eventManager.complete(scheduleId, LocalDate.of(2024, 6, 30)))
                .withMessage("Event is already completed");
        }
    }

    @Nested
    class uncomplete {
        @Test
        void deletesEvent() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            UUID eventId = UUID.fromString("ebb5c232-2f2c-4c08-a2b6-d5ccc81ac08d");
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThat(repository.existsById(eventId)).isTrue();
            eventManager.uncomplete(eventId);
            assertThat(repository.existsById(eventId)).isFalse();
        }

        @Test
        void emitsUnpublishEvent() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            UUID eventId = UUID.fromString("ebb5c232-2f2c-4c08-a2b6-d5ccc81ac08d");
            when(userManager.findCurrentUser()).thenReturn(user);
            eventManager.uncomplete(eventId);
            Optional<EventUncompletedEvent> event = events.stream(EventUncompletedEvent.class).findAny();
            assertThat(event).contains(new EventUncompletedEvent(
                eventId,
                user.id(),
                UUID.fromString("6ba61df2-ab46-4909-b9e6-233ea47dd701"),
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                LocalDateTime.of(2024, 6, 30, 10, 0),
                LocalDateTime.of(2024, 6, 30, 10, 1),
                new BigDecimal("1")
            ));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            UUID eventId = UUID.fromString("ebb5c232-2f2c-4c08-a2b6-d5ccc81ac08d");
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> eventManager.uncomplete(eventId))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfEventNotFound() {
            var user = new UserDTO(
                UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            UUID eventId = UUID.fromString("8cb03ee4-b6e4-4339-a186-7946612d5655");
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(CompletedEventNotFoundException.class)
                .isThrownBy(() -> eventManager.uncomplete(eventId))
                .withMessage("Completed event with ID '8cb03ee4-b6e4-4339-a186-7946612d5655' does not exist");
        }
    }
}