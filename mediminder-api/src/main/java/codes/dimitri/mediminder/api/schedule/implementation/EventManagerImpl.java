package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.medication.MedicationNotFoundException;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class EventManagerImpl implements EventManager {
    private final MedicationManager medicationManager;
    private final UserManager userManager;
    private final CompletedEventEntityRepository repository;
    private final ScheduleEntityRepository scheduleRepository;
    private final EventMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<EventDTO> findAll(@NotNull LocalDate targetDate) {
        UserDTO user = findCurrentUser();
        return findAll(targetDate, user.id());
    }

    @Override
    public List<EventDTO> findAll(LocalDate targetDate, String userId) {
        List<EventDTO> completedEvents = findAllCompletedEventDTOs(targetDate, userId);
        List<UUID> completedScheduleIds = findCompletedScheduleIds(completedEvents);
        List<EventDTO> uncompletedEvents = findAllUncompletedEventDTOs(targetDate, userId, completedScheduleIds);
        return combineAndSortEvents(completedEvents, uncompletedEvents);
    }

    @Override
    @Transactional
    public EventDTO complete(@NotNull UUID scheduleId, @NotNull LocalDate targetDate) {
        UserDTO user = findCurrentUser();
        LocalDateTime today = userManager.calculateTodayForUser(user.id());
        ScheduleEntity schedule = findScheduleEntity(scheduleId, user);
        MedicationDTO medication = findMedicationOrThrowException(schedule.getMedicationId());
        LocalDateTime targetDateTime = LocalDateTime.of(targetDate, schedule.getTime());
        if (!schedule.isHappeningAt(targetDate)) throw new EventNotFoundException(scheduleId, targetDate);
        validateNotYetCompleted(scheduleId, targetDateTime);
        CompletedEventEntity entity = new CompletedEventEntity(
            user.id(),
            schedule,
            targetDateTime,
            today,
            schedule.getDose()
        );
        publishCompletionEvent(entity);
        return mapper.toDTOFromCompletedEvent(repository.save(entity), medication);
    }

    private void publishCompletionEvent(CompletedEventEntity entity) {
        EventCompletedEvent event = mapper.toCompletedEvent(entity);
        eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional
    public void uncomplete(@NotNull UUID eventId) {
        UserDTO currentUser = findCurrentUser();
        CompletedEventEntity entity = findCompletedEvent(eventId, currentUser);
        repository.delete(entity);
        publishUncompletionEvent(entity);
    }

    private void publishUncompletionEvent(CompletedEventEntity entity) {
        EventUncompletedEvent event = mapper.toUncompletedEvent(entity);
        eventPublisher.publishEvent(event);
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidEventException("User is not authenticated", ex);
        }
    }

    private List<EventDTO> findAllCompletedEventDTOs(LocalDate date, String userId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository
            .findByUserIdAndTargetDate(userId, start, end)
            .stream()
            .map(event -> {
                MedicationDTO medication = findMedication(event.getSchedule().getMedicationId(), userId);
                return mapper.toDTOFromCompletedEvent(event, medication);
            })
            .toList();
    }

    private CompletedEventEntity findCompletedEvent(UUID id, UserDTO currentUser) {
        return repository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new CompletedEventNotFoundException(id));
    }

    private static List<EventDTO> combineAndSortEvents(List<EventDTO> completedEvents, List<EventDTO> uncompletedEvents) {
        return Stream
            .concat(completedEvents.stream(), uncompletedEvents.stream())
            .sorted(comparing(EventDTO::targetDate))
            .toList();
    }


    private List<EventDTO> findAllUncompletedEventDTOs(LocalDate date, String userId, Collection<UUID> completedScheduleIds) {
        Specification<ScheduleEntity> query = Specification.allOf(
            ScheduleSpecifications.userId(userId),
            ScheduleSpecifications.onlyActive(true, date)
        );
        List<ScheduleEntity> schedules = scheduleRepository.findAll(query);
        return schedules.stream()
            .filter(schedule -> !completedScheduleIds.contains(schedule.getId()))
            .filter(entity -> entity.isHappeningAt(date))
            .map(entity -> {
                MedicationDTO medication = findMedication(entity.getMedicationId(), userId);
                return mapper.toDTOFromUncompletedSchedule(entity, LocalDateTime.of(date, entity.getTime()), medication);
            })
            .toList();
    }

    private List<UUID> findCompletedScheduleIds(List<EventDTO> completedEvents) {
        return completedEvents.stream().map(EventDTO::scheduleId).distinct().toList();
    }

    private ScheduleEntity findScheduleEntity(UUID id, UserDTO currentUser) {
        return scheduleRepository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    private MedicationDTO findMedication(UUID medicationId, String userId) {
        try {
            return medicationManager.findByIdAndUserId(medicationId, userId);
        } catch (MedicationNotFoundException ex) {
            return null;
        }
    }

    private MedicationDTO findMedicationOrThrowException(UUID medicationid) {
        try {
            return medicationManager.findByIdForCurrentUser(medicationid);
        } catch (MedicationNotFoundException ex) {
            throw new InvalidEventException("Medication is not found", ex);
        }
    }

    private void validateNotYetCompleted(UUID scheduleId, LocalDateTime targetDate) {
        if (repository.existsByScheduleIdAndTargetDate(scheduleId, targetDate)) {
            throw new InvalidEventException("Event is already completed");
        }
    }
}
