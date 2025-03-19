package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
        List<EventDTO> completedEvents = findAllCompletedEventDTOs(targetDate, user);
        List<UUID> completedScheduleIds = findCompletedScheduleIds(completedEvents);
        List<EventDTO> uncompletedEvents = findAllUncompletedEventDTOs(targetDate, user, completedScheduleIds);
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
        return userManager
            .findCurrentUserOptional()
            .orElseThrow(() -> new InvalidEventException("User is not authenticated"));
    }

    private List<EventDTO> findAllCompletedEventDTOs(LocalDate date, UserDTO user) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return repository
            .findByUserIdAndTargetDate(user.id(), start, end)
            .stream()
            .map(event -> {
                MedicationDTO medication = findMedication(event.getSchedule().getMedicationId());
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


    private List<EventDTO> findAllUncompletedEventDTOs(LocalDate date, UserDTO user, Collection<UUID> completedScheduleIds) {
        List<ScheduleEntity> schedules = scheduleRepository.findAllByUserIdWithDateInPeriod(user.id(), date);
        return schedules.stream()
            .filter(schedule -> !completedScheduleIds.contains(schedule.getId()))
            .filter(entity -> entity.isHappeningAt(date))
            .map(entity -> {
                MedicationDTO medication = findMedication(entity.getMedicationId());
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

    private MedicationDTO findMedication(UUID medicationid) {
        return medicationManager
            .findByIdForCurrentUserOptional(medicationid)
            .orElse(null);
    }

    private MedicationDTO findMedicationOrThrowException(UUID medicationid) {
        return medicationManager
            .findByIdForCurrentUserOptional(medicationid)
            .orElseThrow(() -> new InvalidEventException("Medication is not found"));
    }

    private void validateNotYetCompleted(UUID scheduleId, LocalDateTime targetDate) {
        if (repository.existsByScheduleIdAndTargetDate(scheduleId, targetDate)) {
            throw new InvalidEventException("Event is already completed");
        }
    }
}
