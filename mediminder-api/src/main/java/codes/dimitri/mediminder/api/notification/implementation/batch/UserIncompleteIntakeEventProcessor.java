package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.schedule.EventDTO;
import codes.dimitri.mediminder.api.schedule.EventManager;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.function.Predicate.not;

@Component
@RequiredArgsConstructor
public class UserIncompleteIntakeEventProcessor implements ItemProcessor<UserDTO, List<NotificationEntity>> {
    private final UserManager userManager;
    private final EventManager eventManager;
    private final NotificationProperties properties;
    private final Clock clock;

    @Override
    public List<NotificationEntity> process(UserDTO user) {
        LocalDateTime today = userManager.calculateTodayForUser(user.id());
        List<EventDTO> events = eventManager.findAll(today.toLocalDate(), user.id());
        return events
            .stream()
            .filter(not(EventDTO::isCompleted))
            .filter(event -> isEventInTimeWindow(event, today))
            .map(event -> createIntakeNotification(user.id(), event))
            .toList();
    }

    private boolean isEventInTimeWindow(EventDTO event, LocalDateTime today) {
        LocalDateTime minTime = today.minus(properties.intake().warnPeriod());
        LocalDateTime maxTime = today.plus(properties.intake().warnPeriod());
        return !minTime.isAfter(event.targetDate()) && !maxTime.isBefore(event.targetDate());
    }

    private NotificationEntity createIntakeNotification(String userId, EventDTO item) {
        return new NotificationEntity(
            userId,
            NotificationType.INTAKE_EVENT,
            item.scheduleId(),
            "Time to take your medicine",
            "You have to take '" + item.medication().name() + "' at " + item.targetDate().toLocalTime(),
            Instant.now(clock).plus(properties.intake().lifetime())
        );
    }
}
