package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import lombok.Value;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ScheduleManagerImpl.class, ScheduleEntityMapperImpl.class})
@ContextConfiguration(classes = ValidationAutoConfiguration.class)
class ScheduleManagerImplTest {
    @Autowired
    private ScheduleManagerImpl manager;
    @MockBean
    private ScheduleEntityRepository repository;
    @MockBean
    private MedicationManager medicationManager;
    @MockBean
    private UserManager userManager;
    @Captor
    private ArgumentCaptor<ScheduleEntity> anyEntity;

    @Nested
    class findAllForCurrentUser {
        @Test
        void returnsResult() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .create();
            var pageRequest = PageRequest.of(0, 10);
            // When
            when(repository.findAllByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(schedule)));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            // Then
            Page<ScheduleDTO> results = manager.findAllForCurrentUser(pageRequest);
            assertThat(results.getContent()).containsOnly(new ScheduleDTO(
                schedule.getId(),
                schedule.getUserId(),
                medication,
                schedule.getInterval(),
                new SchedulePeriodDTO(schedule.getPeriod().getStartingAt(), schedule.getPeriod().getEndingAtInclusive()),
                schedule.getDescription(),
                schedule.getDose(),
                schedule.getTime()
            ));
        }

        @Test
        void retrievesData() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .create();
            var pageRequest = PageRequest.of(0, 10);
            // When
            when(repository.findAllByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(schedule)));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            // Then
            manager.findAllForCurrentUser(pageRequest);
            verify(repository).findAllByUserId(user.id(), pageRequest);
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .create();
            var pageRequest = PageRequest.of(0, 10);
            // When
            when(repository.findAllByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(schedule)));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(pageRequest))
                .withMessage("User is not authenticated");
        }

        @Test
        void returnsScheduleWithoutMedicationIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            var pageRequest = PageRequest.of(0, 10);
            // When
            when(repository.findAllByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(schedule)));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            Page<ScheduleDTO> results = manager.findAllForCurrentUser(pageRequest);
            assertThat(results.getContent()).containsOnly(new ScheduleDTO(
                schedule.getId(),
                schedule.getUserId(),
                null,
                schedule.getInterval(),
                new SchedulePeriodDTO(schedule.getPeriod().getStartingAt(), schedule.getPeriod().getEndingAtInclusive()),
                schedule.getDescription(),
                schedule.getDose(),
                schedule.getTime()
            ));
        }
    }

    @Nested
    class createForCurrentUser {
        @Test
        void returnsResult() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .set(field(CreateScheduleRequestDTO::medicationId), medication.id())
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            ScheduleDTO result = manager.createForCurrentUser(request);
            verify(repository).save(anyEntity.capture());
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
            assertThat(result).isEqualTo(new ScheduleDTO(
                anyEntity.getValue().getId(),
                user.id(),
                medication,
                request.interval(),
                new SchedulePeriodDTO(request.period().startingAt(), request.period().endingAtInclusive()),
                request.description(),
                request.dose(),
                request.time()
            ));
        }

        @Test
        void savesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .set(field(CreateScheduleRequestDTO::medicationId), medication.id())
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.createForCurrentUser(request);
            verify(repository).save(anyEntity.capture());
            assertThat(anyEntity.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new ScheduleEntity(
                    user.id(),
                    medication.id(),
                    new SchedulePeriodEntity(request.period().startingAt(), request.period().endingAtInclusive()),
                    request.interval(),
                    request.time(),
                    request.description(),
                    request.dose()
                ));
        }

        @Test
        void throwsExceptionIfEndDateBeforeStartDate() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .set(field(CreateScheduleRequestDTO::medicationId), medication.id())
                .set(field(SchedulePeriodDTO::startingAt), LocalDate.of(2024, 6, 30))
                .set(field(SchedulePeriodDTO::endingAtInclusive), LocalDate.of(2024, 6, 1))
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("End date has to go after the start date when given");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfMedicationNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Medication is not found");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfMedicationNotGiven() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(CreateScheduleRequestDTO::medicationId))
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Medication is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfIntervalNotGiven() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(CreateScheduleRequestDTO::interval))
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Interval is required");
            verifyNoInteractions(repository);
        }

        @ParameterizedTest
        @CsvSource({"0", "-1"})
        void throwsExceptionIfIntervalIsZeroOrNegative(int days) {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .set(field(CreateScheduleRequestDTO::interval), Period.ofDays(days))
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Interval must be positive");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfPeriodNotGiven() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(CreateScheduleRequestDTO::period))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Period is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfStartingAtNotGiven() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::startingAt))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("The starting date is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfTimeNotGiven() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(CreateScheduleRequestDTO::time))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Time is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfDescriptionTooLong() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .generate(field(CreateScheduleRequestDTO::description), gen -> gen.string().length(129))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Description should not contain more than 128 characters");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfDoseNotGiven() {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .ignore(field(CreateScheduleRequestDTO::dose))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Dose is required");
            verifyNoInteractions(repository);
        }

        @ParameterizedTest
        @CsvSource({"-1", "0"})
        void throwsExceptionIfDoseIsZeroOrNegative(BigDecimal dose) {
            // Given
            var request = Instancio.of(CreateScheduleRequestDTO.class)
                .set(field(CreateScheduleRequestDTO::dose), dose)
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Dose should be positive");
            verifyNoInteractions(repository);
        }
    }


    @Nested
    class updateForCurrentUser {
        @Test
        void returnsResult() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            ScheduleDTO result = manager.updateForCurrentUser(entity.getId(), request);
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            assertThat(result).isEqualTo(new ScheduleDTO(
                entity.getId(),
                user.id(),
                medication,
                request.interval(),
                new SchedulePeriodDTO(request.period().startingAt(), request.period().endingAtInclusive()),
                request.description(),
                request.dose(),
                request.time()
            ));
        }

        @Test
        void updatesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.updateForCurrentUser(entity.getId(), request);
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new ScheduleEntity(
                    entity.getId(),
                    user.id(),
                    medication.id(),
                    new SchedulePeriodEntity(request.period().startingAt(), request.period().endingAtInclusive()),
                    request.interval(),
                    request.time(),
                    request.description(),
                    request.dose()
                ));
        }

        @Test
        void throwsExceptionIfEntityNotFound() {
            // Given
            var id = UUID.randomUUID();
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(ScheduleNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Schedule with ID '" + id + "' does not exist");
        }

        @Test
        void throwsExceptionIfEndDateBeforeStartDate() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .set(field(SchedulePeriodDTO::startingAt), LocalDate.of(2024, 6, 30))
                .set(field(SchedulePeriodDTO::endingAtInclusive), LocalDate.of(2024, 6, 1))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(entity.getId(), request))
                .withMessage("End date has to go after the start date when given");
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfMedicationNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(entity.getId(), request))
                .withMessage("Medication is not found");
        }

        @Test
        void throwsExceptionIfIntervalNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(UpdateScheduleRequestDTO::interval))
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Interval is required");
            verifyNoInteractions(repository);
        }

        @ParameterizedTest
        @CsvSource({"0", "-1"})
        void throwsExceptionIfIntervalIsZeroOrNegative(int days) {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .set(field(UpdateScheduleRequestDTO::interval), Period.ofDays(days))
                .ignore(field(SchedulePeriodDTO::endingAtInclusive))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Interval must be positive");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfPeriodNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(UpdateScheduleRequestDTO::period))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Period is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfStartingAtNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(SchedulePeriodDTO::startingAt))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("The starting date is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfTimeNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(UpdateScheduleRequestDTO::time))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Time is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfDescriptionTooLong() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .generate(field(UpdateScheduleRequestDTO::description), gen -> gen.string().length(129))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Description should not contain more than 128 characters");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfDoseNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .ignore(field(UpdateScheduleRequestDTO::dose))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Dose is required");
            verifyNoInteractions(repository);
        }

        @ParameterizedTest
        @CsvSource({"-1", "0"})
        void throwsExceptionIfDoseIsZeroOrNegative(BigDecimal dose) {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateScheduleRequestDTO.class)
                .set(field(UpdateScheduleRequestDTO::dose), dose)
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Dose should be positive");
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class deleteForCurrentUser {

        @Test
        void deletesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.deleteForCurrentUser(entity.getId());
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            verify(repository).delete(entity);
        }

        @Test
        void throwsExceptionIfEntityNotFound() {
            // Given
            var id = UUID.randomUUID();
            var user = Instancio.create(UserDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(ScheduleNotFoundException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("Schedule with ID '" + id + "' does not exist");
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }
    }

    @Test
    void calculateRequiredDoses() {
        // Given
        var startingAt = LocalDate.of(2024, 6, 1);
        var endingAtInclusive = LocalDate.of(2024, 6, 30);
        var id = UUID.randomUUID();
        var period = new SchedulePeriodDTO(startingAt, endingAtInclusive);
        var schedule1 = Instancio.of(ScheduleEntity.class)
            .set(field(ScheduleEntity::getPeriod), SchedulePeriodEntity.ofUnboundedEnd(LocalDate.of(2024, 6, 5)))
            .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
            .set(field(ScheduleEntity::getDose), BigDecimal.ONE)
            .create();
        var schedule2 = Instancio.of(ScheduleEntity.class)
            .set(field(ScheduleEntity::getPeriod), SchedulePeriodEntity.of(
                LocalDate.of(2024, 6, 10),
                LocalDate.of(2024, 6, 21)))
            .set(field(ScheduleEntity::getInterval), Period.ofDays(2))
            .set(field(ScheduleEntity::getDose), BigDecimal.ONE)
            .create();
        // When
        when(repository.findAllByMedicationIdAndDateInPeriodGroup(any(), any(), any())).thenReturn(List.of(schedule1, schedule2));
        // Then
        BigDecimal result = manager.calculateRequiredDoses(id, period);
        assertThat(result).isEqualByComparingTo("32");
        verify(repository).findAllByMedicationIdAndDateInPeriodGroup(startingAt, endingAtInclusive, id);
    }

    @Test
    void deleteAllByMedicationId() {
        // Given
        var id = UUID.randomUUID();
        // When
        manager.deleteAllByMedicationId(id);
        // Then
        verify(repository).deleteAllByMedicationId(id);
    }

    @Nested
    class findByIdForCurrentUser {
        @Test
        void returnsDTO() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            ScheduleDTO result = manager.findByIdForCurrentUser(entity.getId());
            assertThat(result).isEqualTo(new ScheduleDTO(
                entity.getId(),
                entity.getUserId(),
                medication,
                entity.getInterval(),
                new SchedulePeriodDTO(entity.getPeriod().getStartingAt(), entity.getPeriod().getEndingAtInclusive()),
                entity.getDescription(),
                entity.getDose(),
                entity.getTime()
            ));
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
        }

        @Test
        void returnsDTOWithoutMedicationIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getUserId), user.id())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            ScheduleDTO result = manager.findByIdForCurrentUser(entity.getId());
            assertThat(result).isEqualTo(new ScheduleDTO(
                entity.getId(),
                entity.getUserId(),
                null,
                entity.getInterval(),
                new SchedulePeriodDTO(entity.getPeriod().getStartingAt(), entity.getPeriod().getEndingAtInclusive()),
                entity.getDescription(),
                entity.getDose(),
                entity.getTime()
            ));
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidScheduleException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
        }
    }

    @Test
    void findAllUserScheduledMedicationOnDate() {
        // Given
        var date = LocalDate.of(2024, 6, 1);
        var pageRequest = PageRequest.of(0, 10);
        var userScheduleMedication = Instancio.create(UserScheduledMedicationImpl.class);
        // When
        when(repository.findAllWithUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduleMedication)));
        // Then
        Page<UserScheduledMedicationDTO> results = manager.findAllUserScheduledMedicationOnDate(date, pageRequest);
        assertThat(results).containsOnly(new UserScheduledMedicationDTO(
            userScheduleMedication.getUserId(),
            userScheduleMedication.getMedicationId()
        ));
        verify(repository).findAllWithUserScheduledMedicationOnDate(date, pageRequest);
    }

    @Test
    void findAllWithinPeriod() {
        // Given
        var pageRequest = PageRequest.of(0, 10);
        var startingAt = LocalDate.of(2024, 6, 1);
        var endingAtInclusive = LocalDate.of(2024, 6, 30);
        var period = new SchedulePeriodDTO(startingAt, endingAtInclusive);
        var medication = Instancio.create(MedicationDTO.class);
        var entity = Instancio.of(ScheduleEntity.class)
            .set(field(ScheduleEntity::getMedicationId), medication.id())
            .create();
        // When
        when(repository.findAllByOverlappingPeriod(any(), any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        // Then
        Page<ScheduleDTO> results = manager.findAllWithinPeriod(period, pageRequest);
        assertThat(results).containsOnly(new ScheduleDTO(
            entity.getId(),
            entity.getUserId(),
            medication,
            entity.getInterval(),
            new SchedulePeriodDTO(entity.getPeriod().getStartingAt(), entity.getPeriod().getEndingAtInclusive()),
            entity.getDescription(),
            entity.getDose(),
            entity.getTime()
        ));
        verify(repository).findAllByOverlappingPeriod(startingAt, endingAtInclusive, pageRequest);
        verify(medicationManager).findByIdAndUserId(entity.getMedicationId(), entity.getUserId());
    }

    @Value
    static class UserScheduledMedicationImpl implements UserScheduledMedication {
        UUID userId;
        UUID medicationId;
    }
}