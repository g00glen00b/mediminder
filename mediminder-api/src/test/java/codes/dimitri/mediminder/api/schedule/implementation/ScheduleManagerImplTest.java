package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
@Sql("classpath:test-data/schedules.sql")
@Sql(value = "classpath:test-data/cleanup-schedules.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ScheduleManagerImplTest {
    @Autowired
    private ScheduleManager manager;
    @Autowired
    private ScheduleEntityRepository repository;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private MedicationManager medicationManager;

    @Nested
    class findAllForCurrentUser {
        @Test
        void returnsResults() {
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(medication);
            var schedules = manager.findAllForCurrentUser(null, pageRequest);
            assertThat(schedules).containsExactly(new ScheduleDTO(
                UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3"),
                user.id(),
                medication,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                "Before breakfast",
                new BigDecimal("1"),
                LocalTime.of(10, 0)
            ));
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
        }

        @Test
        void returnsResultsForMedicationId() {
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(medication);
            var schedules = manager.findAllForCurrentUser(medication.id(), pageRequest);
            assertThat(schedules).containsExactly(new ScheduleDTO(
                UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3"),
                user.id(),
                medication,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                "Before breakfast",
                new BigDecimal("1"),
                LocalTime.of(10, 0)
            ));
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null, pageRequest))
                .withMessage("User is not authenticated");
        }

        @Test
        void usesEmptyMedicationIfNotFound() {
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(user);
            var schedules = manager.findAllForCurrentUser(null, pageRequest);
            assertThat(schedules).containsExactly(new ScheduleDTO(
                UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3"),
                user.id(),
                null,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                "Before breakfast",
                new BigDecimal("1"),
                LocalTime.of(10, 0)
            ));
        }

        @Test
        void failsIfPageableNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null, null));
        }
    }

    @Nested
    class createForCurrentUser {
        @Test
        void returnsResult() {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var request = new CreateScheduleRequestDTO(
                medication.id(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            var schedule = manager.createForCurrentUser(request);
            assertThat(schedule).isEqualTo(new ScheduleDTO(
                schedule.id(),
                user.id(),
                medication,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                "Before breakfast",
                new BigDecimal("1"),
                LocalTime.of(10, 0)
            ));
        }

        @Test
        void savesEntity() {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var request = new CreateScheduleRequestDTO(
                medication.id(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            var schedule = manager.createForCurrentUser(request);
            ScheduleEntity entity = repository.findById(schedule.id()).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new ScheduleEntity(
                    schedule.id(),
                    user.id(),
                    medication.id(),
                    SchedulePeriodEntity.of(LocalDate.of(2024, 6, 30), null),
                    Period.ofDays(1),
                    LocalTime.of(10, 0),
                    "Before breakfast",
                    new BigDecimal("1")
                ));
        }

        @Test
        void failsIfMedicationNotFound() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.medicationId(), user.id())).thenThrow(new MedicationNotFoundException(request.medicationId()));
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Medication is not found");
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(null));
        }

        @Test
        void failsIfMedicationNotGiven() {
            var request = new CreateScheduleRequestDTO(
                null,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Medication is required");
        }

        @Test
        void failsIfIntervalNotGiven() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                null,
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Interval is required");
        }

        @Test
        void failsIfIntervalIsNegative() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(-1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Interval must be positive");
        }

        @Test
        void failsIfPeriodIsMissing() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                null,
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Period is required");
        }

        @Test
        void failsIfStartDateNotGiven() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    null,
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("The starting date is required");
        }

        @Test
        void failsIfTimeNotGiven() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                null,
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Time is required");
        }

        @Test
        void failsIfDoseNotGiven() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                null
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Dose is required");
        }

        @Test
        void failsIfDoseIsNegative() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("-1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Dose should be positive");
        }

        @Test
        void failsIfDescriptionTooLong() {
            var request = new CreateScheduleRequestDTO(
                null,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                LocalTime.of(10, 0),
                "a".repeat(129),
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Description should not contain more than 128 characters");
        }

        @Test
        void failsIfEndDateBeforeStartDate() {
            var request = new CreateScheduleRequestDTO(
                UUID.randomUUID(),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    LocalDate.of(2024, 6, 20)
                ),
                LocalTime.of(10, 0),
                "Before breakfast",
                new BigDecimal("1")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("End date must be after the starting date");
        }
    }

    @Nested
    class updateForCurrentUser {
        @Test
        void returnsResult() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var medication = new MedicationDTO(
                UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            ScheduleDTO result = manager.updateForCurrentUser(id, request);
            assertThat(result).isEqualTo(new ScheduleDTO(
                id,
                user.id(),
                medication,
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                "After breakfast",
                new BigDecimal("2"),
                LocalTime.of(9, 0)
            ));
        }

        @Test
        void savesEntity() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var medication = new MedicationDTO(
                UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            manager.updateForCurrentUser(id, request);
            ScheduleEntity entity = repository.findById(id).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new ScheduleEntity(
                    id,
                    user.id(),
                    medication.id(),
                    SchedulePeriodEntity.of(LocalDate.of(2024, 6, 1), null),
                    Period.ofDays(2),
                    LocalTime.of(9, 0),
                    "After breakfast",
                    new BigDecimal("2")
                ));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfMedicationNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var medicationId = UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medicationId, user.id())).thenThrow(new MedicationNotFoundException(medicationId));
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Medication is not found");
        }

        @Test
        void failsIfScheduleNotFound() {
            UUID id = UUID.fromString("08a6aa16-8449-418e-93ff-c7975731066d");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(ScheduleNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Schedule with ID '08a6aa16-8449-418e-93ff-c7975731066d' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(null, request));
        }

        @Test
        void failsIfRequestNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, null));
        }

        @Test
        void failsIfIntervalNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                null,
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Interval is required");
        }

        @Test
        void failsIfIntervalIsNegative() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(-1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Interval must be positive");
        }

        @Test
        void failsIfPeriodNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                null,
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Period is required");
        }

        @Test
        void failsIfPeriodIsNegative() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 5, 30)
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("End date must be after the starting date");
        }

        @Test
        void failsIfStartingAtNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    null,
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("The starting date is required");
        }

        @Test
        void failsIfTimeNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                null,
                "After breakfast",
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Time is required");
        }

        @Test
        void failsIfDescriptionTooLong() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "a".repeat(129),
                new BigDecimal("2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Description should not contain more than 128 characters");
        }

        @Test
        void failsIfDoseNotGiven() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                null
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Dose is required");
        }

        @Test
        void failsIfDoseIsNegative() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(2),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 1),
                    null
                ),
                LocalTime.of(9, 0),
                "After breakfast",
                new BigDecimal("-2")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Dose should be positive");
        }
    }

    @Nested
    class deleteForCurrentUser {
        @Test
        void deletesEntity() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            manager.deleteForCurrentUser(id);
            assertThat(repository.findById(id)).isEmpty();
        }

        @Test
        void failsIfUserNotAuthenticated() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfEntityNotFound() {
            UUID id = UUID.fromString("08a6aa16-8449-418e-93ff-c7975731066d");
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(ScheduleNotFoundException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("Schedule with ID '08a6aa16-8449-418e-93ff-c7975731066d' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(null));
        }
    }

    @Nested
    class calculateRequiredDoses {
        @ParameterizedTest
        @CsvSource({
            "0b845403-3b16-436f-b84a-925b01421ad9,2024-06-01,2024-06-30,31",
            "0b845403-3b16-436f-b84a-925b01421ad9,2024-05-01,2024-05-31,1",
            "0b845403-3b16-436f-b84a-925b01421ad9,2024-05-01,2024-05-30,0",
            "00000000-0000-0000-0000-000000000000,2024-01-01,2024-12-31,0"
        })
        void returnsResult(UUID id, LocalDate startingAt, LocalDate endingAt, String expected) {
            var period = new SchedulePeriodDTO(
                startingAt,
                endingAt
            );
            BigDecimal result = manager.calculateRequiredDoses(id, period);
            assertThat(result).isEqualByComparingTo(expected);
        }

        @Test
        void failsIfMedicationIdNotGiven() {
            var period = new SchedulePeriodDTO(
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 30)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.calculateRequiredDoses(null, period));
        }

        @Test
        void failsIfPeriodNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.calculateRequiredDoses(UUID.randomUUID(), null));
        }
    }

    @Nested
    class deleteAllByMedicationId {
        @Test
        void deletesEntities() {
            UUID medicationId = UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9");
            UUID userId = UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330");
            var pageRequest = PageRequest.of(0, 10);
            manager.deleteAllByMedicationId(medicationId);
            assertThat(repository.findAllByUserId(userId, pageRequest)).hasSize(1);
        }

        @Test
        void failsIfMedicationIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.deleteAllByMedicationId(null));
        }
    }

    @Nested
    class findByIdForCurrentUser {
        @Test
        void returnsResult() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            var schedule = manager.findByIdForCurrentUser(id);
            assertThat(schedule).isEqualTo(new ScheduleDTO(
                id,
                user.id(),
                medication,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                "Before breakfast",
                new BigDecimal("1"),
                LocalTime.of(10, 0)
            ));
        }

        @Test
        void usesMedicationNullIfNotFound() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            var schedule = manager.findByIdForCurrentUser(id);
            assertThat(schedule).isEqualTo(new ScheduleDTO(
                id,
                user.id(),
                null,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 6, 30),
                    null
                ),
                "Before breakfast",
                new BigDecimal("1"),
                LocalTime.of(10, 0)
            ));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            UUID id = UUID.fromString("945b1bea-b447-4701-a137-3e447c35ffa3");
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfEntityNotFound() {
            UUID id = UUID.fromString("08a6aa16-8449-418e-93ff-c7975731066d");
            var user = new UserDTO(
                UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                "Harry Potter",
                ZoneId.of("UTC"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(ScheduleNotFoundException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("Schedule with ID '08a6aa16-8449-418e-93ff-c7975731066d' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(null));
        }
    }

    @Nested
    class findAllUserScheduledMedicationOnDate {
        @ParameterizedTest
        @CsvSource({
            "2024-06-01,1",
            "2024-06-30,3",
            "2024-08-01,2"
        })
        void returnsCorrectNumberOfResults(LocalDate targetDate, int expectedResults) {
            var pageRequest = PageRequest.of(0, 10);
            Page<UserScheduledMedicationDTO> results = manager.findAllUserScheduledMedicationOnDate(targetDate, pageRequest);
            assertThat(results).hasSize(expectedResults);
        }

        @Test
        void returnsResult() {
            var pageRequest = PageRequest.of(0, 10);
            var targetDate = LocalDate.of(2024, 6, 30);
            Page<UserScheduledMedicationDTO> results = manager.findAllUserScheduledMedicationOnDate(targetDate, pageRequest);
            assertThat(results).containsOnlyOnce(
                new UserScheduledMedicationDTO(
                    UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                    UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9")),
                new UserScheduledMedicationDTO(
                    UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330"),
                    UUID.fromString("a9356fca-da82-48ab-af04-a7169b91ea4f")),
                new UserScheduledMedicationDTO(
                    UUID.fromString("b47e0b6f-be52-4e38-8301-fe60d08cbfbe"),
                    UUID.fromString("fb384363-0446-4fdc-a62d-098c20ddf286"))
            );
        }

        @Test
        void failsIfTargetDateNotGiven() {
            var pageRequest = PageRequest.of(0, 10);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllUserScheduledMedicationOnDate(null, pageRequest));
        }

        @Test
        void failsIfPageableNotGiven() {
            var targetDate = LocalDate.of(2024, 6, 30);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllUserScheduledMedicationOnDate(targetDate, null));
        }
    }

    @Nested
    class findAllWithinPeriod {
        @Test
        void returnsResults() {
            var period = new SchedulePeriodDTO(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31)
            );
            var medication = new MedicationDTO(
                UUID.fromString("0b845403-3b16-436f-b84a-925b01421ad9"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var userId = UUID.fromString("9133c9d2-0b6c-4915-9752-512d2dca9330");
            var pageRequest = PageRequest.of(0, 10);
            when(medicationManager.findByIdAndUserId(medication.id(), userId)).thenReturn(medication);
            Page<ScheduleDTO> results = manager.findAllWithinPeriod(period, pageRequest);
            assertThat(results).containsExactly(new ScheduleDTO(
                UUID.fromString("f2f2de45-3000-45fc-af12-fa8cfce5c2ff"),
                userId,
                medication,
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2024, 5, 31),
                    LocalDate.of(2024, 7, 31)
                ),
                "After dinner",
                new BigDecimal("1"),
                LocalTime.of(18, 0)
            ));
        }

        @ParameterizedTest
        @CsvSource({
            "2024-05-01,2024-05-31,1",
            "2024-05-01,2024-05-30,0",
            "2024-05-01,2024-06-30,4",
            "2024-08-01,2024-08-31,2"
        })
        void returnsCorrectNumberOfResults(LocalDate startingAt, LocalDate endingAt, int expectedResults) {
            var period = new SchedulePeriodDTO(startingAt, endingAt);
            var pageRequest = PageRequest.of(0, 10);
            Page<ScheduleDTO> results = manager.findAllWithinPeriod(period, pageRequest);
            assertThat(results).hasSize(expectedResults);
        }

        @Test
        void failsIfPeriodNotGiven() {
            var pageRequest = PageRequest.of(0, 10);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllWithinPeriod(null, pageRequest));
        }

        @Test
        void failsIfPageableNotGiven() {
            var period = new SchedulePeriodDTO(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllWithinPeriod(period, null));
        }
    }
}