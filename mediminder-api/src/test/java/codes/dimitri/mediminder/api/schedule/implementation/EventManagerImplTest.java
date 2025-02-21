package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

@SpringBootTest(classes = {EventManagerImpl.class, EventMapperImpl.class})
@ContextConfiguration(classes = ValidationAutoConfiguration.class)
@RecordApplicationEvents
class EventManagerImplTest {
    @Autowired
    private EventManagerImpl manager;
    @Autowired
    private ApplicationEvents events;
    @MockBean
    private MedicationManager medicationManager;
    @MockBean
    private UserManager userManager;
    @MockBean
    private CompletedEventEntityRepository repository;
    @MockBean
    private ScheduleEntityRepository scheduleRepository;
    @Captor
    private ArgumentCaptor<CompletedEventEntity> anyEntity;
    @Captor
    private ArgumentCaptor<EventCompletedEvent> anyCompletedEvent;

    @Nested
    class findAll {
        @Test
        void returnsResults() {
            // Given
            var targetDateTime = LocalDateTime.of(2024, 6, 10, 10, 0);
            var user = Instancio.create(UserDTO.class);
            var medication1 = Instancio.create(MedicationDTO.class);
            var medication2 = Instancio.create(MedicationDTO.class);
            var schedule1 = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication1.id())
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDateTime.toLocalDate().minusDays(1))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            var schedule2 = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication2.id())
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDateTime.toLocalDate().minusDays(2))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            var completedEvent = Instancio.of(CompletedEventEntity.class)
                .set(field(CompletedEventEntity::getSchedule), schedule1)
                .set(field(CompletedEventEntity::getTargetDate), targetDateTime)
                .create();
            // When
            when(repository.findByUserIdAndTargetDate(any(), any(), any())).thenReturn(List.of(completedEvent));
            when(scheduleRepository.findAllByUserIdWithDateInPeriod(any(), any())).thenReturn(List.of(schedule1, schedule2));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(medication1.id())).thenReturn(Optional.of(medication1));
            when(medicationManager.findByIdForCurrentUser(medication2.id())).thenReturn(Optional.of(medication2));
            // Then
            List<EventDTO> results = manager.findAll(targetDateTime.toLocalDate());
            assertThat(results).containsOnly(new EventDTO(
                completedEvent.getId(),
                schedule1.getId(),
                medication1,
                completedEvent.getTargetDate(),
                completedEvent.getCompletedDate(),
                completedEvent.getDose(),
                schedule1.getDescription()
            ), new EventDTO(
                null,
                schedule2.getId(),
                medication2,
                LocalDateTime.of(targetDateTime.toLocalDate(), schedule2.getTime()),
                null,
                schedule2.getDose(),
                schedule2.getDescription()
            ));
        }

        @Test
        void retrievesData() {
            // Given
            var targetDateTime = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = targetDateTime.toLocalDate();
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDate.minusDays(1))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            var completedEvent = Instancio.of(CompletedEventEntity.class)
                .set(field(CompletedEventEntity::getSchedule), schedule)
                .set(field(CompletedEventEntity::getTargetDate), targetDateTime)
                .create();
            // When
            when(repository.findByUserIdAndTargetDate(any(), any(), any())).thenReturn(List.of(completedEvent));
            when(scheduleRepository.findAllByUserIdWithDateInPeriod(any(), any())).thenReturn(List.of(schedule));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(Optional.of(medication));
            // Then
            manager.findAll(targetDate);
            verify(repository).findByUserIdAndTargetDate(user.id(), targetDate.atStartOfDay(), targetDate.plusDays(1).atStartOfDay());
            verify(scheduleRepository).findAllByUserIdWithDateInPeriod(user.id(), targetDate);
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdForCurrentUser(schedule.getMedicationId());
        }

        @Test
        void returnsNoMedicationIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var schedule = Instancio.create(ScheduleEntity.class);
            var completedEvent = Instancio.of(CompletedEventEntity.class)
                .set(field(CompletedEventEntity::getSchedule), schedule)
                .create();
            LocalDate targetDate = completedEvent.getTargetDate().toLocalDate();
            // When
            when(repository.findByUserIdAndTargetDate(any(), any(), any())).thenReturn(List.of(completedEvent));
            when(scheduleRepository.findAllByUserIdWithDateInPeriod(any(), any())).thenReturn(List.of(schedule));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            List<EventDTO> result = manager.findAll(targetDate);
            assertThat(result).containsOnly(new EventDTO(
                completedEvent.getId(),
                schedule.getId(),
                null,
                completedEvent.getTargetDate(),
                completedEvent.getCompletedDate(),
                completedEvent.getDose(),
                schedule.getDescription()
            ));
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var targetDate = LocalDate.of(2024, 6, 30);
            // Then
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> manager.findAll(targetDate))
                .withMessage("User is not authenticated");
        }
    }

    @Nested
    class complete {
        @Test
        void returnsEvent() {
            // Given
            var today = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = today.toLocalDate();
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDate.minusDays(1))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(any())).thenReturn(today);
            when(scheduleRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(schedule));
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            EventDTO result = manager.complete(schedule.getId(), targetDate);
            verify(repository).save(anyEntity.capture());
            assertThat(result).isEqualTo(new EventDTO(
                anyEntity.getValue().getId(),
                schedule.getId(),
                medication,
                LocalDateTime.of(targetDate, schedule.getTime()),
                today,
                schedule.getDose(),
                schedule.getDescription()
            ));
        }

        @Test
        void retrievesData() {
            // Given
            var today = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = today.toLocalDate();
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDate.minusDays(1))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(any())).thenReturn(today);
            when(scheduleRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(schedule));
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            manager.complete(schedule.getId(), targetDate);
            verify(userManager).findCurrentUser();
            verify(userManager).calculateTodayForUser(user.id());
            verify(scheduleRepository).findByIdAndUserId(schedule.getId(), user.id());
            verify(repository).save(anyEntity.capture());
            verify(medicationManager).findByIdForCurrentUser(medication.id());
            assertThat(anyEntity.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new CompletedEventEntity(
                    user.id(),
                    schedule,
                    LocalDateTime.of(targetDate, schedule.getTime()),
                    today,
                    schedule.getDose()
                ));
        }

        @Test
        void publishesEvent() {
            // Given
            var today = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = today.toLocalDate();
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.create(MedicationDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getMedicationId), medication.id())
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDate.minusDays(1))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(any())).thenReturn(today);
            when(scheduleRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(schedule));
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            manager.complete(schedule.getId(), targetDate);
            verify(repository).save(anyEntity.capture());
            Optional<EventCompletedEvent> result = events.stream(EventCompletedEvent.class).findAny();
            assertThat(result).contains(new EventCompletedEvent(
                anyEntity.getValue().getId(),
                user.id(),
                schedule.getId(),
                schedule.getMedicationId(),
                LocalDateTime.of(targetDate, schedule.getTime()),
                today,
                schedule.getDose()
            ));
        }

        @Test
        void throwsExceptionIfMedicationNotFound() {
            // Given
            var today = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = today.toLocalDate();
            var user = Instancio.create(UserDTO.class);
            var schedule = Instancio.of(ScheduleEntity.class)
                .set(field(ScheduleEntity::getInterval), Period.ofDays(1))
                .set(field(SchedulePeriodEntity::getStartingAt), targetDate.minusDays(1))
                .ignore(field(SchedulePeriodEntity::getEndingAtInclusive))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(any())).thenReturn(today);
            when(scheduleRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(schedule));
            // Then
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> manager.complete(schedule.getId(), targetDate))
                .withMessage("Medication is not found");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionScheduleNotFound() {
            // Given
            var today = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = today.toLocalDate();
            var user = Instancio.create(UserDTO.class);
            var scheduleId = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(any())).thenReturn(today);
            // Then
            assertThatExceptionOfType(ScheduleNotFoundException.class)
                .isThrownBy(() -> manager.complete(scheduleId, targetDate))
                .withMessage("Schedule with ID '" + scheduleId + "' does not exist");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var today = LocalDateTime.of(2024, 6, 10, 10, 0);
            var targetDate = today.toLocalDate();
            var scheduleId = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> manager.complete(scheduleId, targetDate))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class delete {
        @Test
        void deletesEntity() {
            // Given
            var event = Instancio.create(CompletedEventEntity.class);
            var user = Instancio.create(UserDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(event));
            // Then
            manager.delete(event.getId());
            verify(userManager).findCurrentUser();
            verify(repository).findByIdAndUserId(event.getId(), user.id());
            verify(repository).delete(event);
        }

        @Test
        void publishesEvent() {
            // Given
            var event = Instancio.create(CompletedEventEntity.class);
            var user = Instancio.create(UserDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(event));
            // Then
            manager.delete(event.getId());
            Optional<EventUncompletedEvent> result = events.stream(EventUncompletedEvent.class).findAny();
            assertThat(result).contains(new EventUncompletedEvent(
                event.getId(),
                event.getUserId(),
                event.getSchedule().getId(),
                event.getSchedule().getMedicationId(),
                event.getTargetDate(),
                event.getCompletedDate(),
                event.getDose()
            ));
        }

        @Test
        void throwsExceptionIfEventNotFound() {
            // Given
            var id = UUID.randomUUID();
            var user = Instancio.create(UserDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(CompletedEventNotFoundException.class)
                .isThrownBy(() -> manager.delete(id))
                .withMessage("Completed event with ID '" + id + "' does not exist");
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidEventException.class)
                .isThrownBy(() -> manager.delete(id))
                .withMessage("User is not authenticated");
        }
    }
}