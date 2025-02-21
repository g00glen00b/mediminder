package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ActiveScheduleNotificationProcessor implements ItemProcessor<ScheduleDTO, NotificationEntity> {
    private final UserManager userManager;
    private final NotificationProperties properties;
    private final Clock clock;

    @Override
    public NotificationEntity process(ScheduleDTO item) {
        if (item.medication() == null) return null;
        LocalDateTime today = userManager.calculateTodayForUser(item.userId());
        if (!isScheduleActiveForUser(item, today)) return null;
        return createIntakeNotification(item);
    }

    private boolean isScheduleActiveForUser(ScheduleDTO schedule, LocalDateTime today) {
        return !isScheduleEndingBeforeDate(schedule, today) &&
            !isScheduleStartingAfterDate(schedule, today) &&
            !isScheduleOutsideTimeWindow(schedule, today);
    }

    private static boolean isScheduleEndingBeforeDate(ScheduleDTO schedule, LocalDateTime today) {
        LocalDate todayDate = today.toLocalDate();
        return schedule.period().endingAtInclusive() != null && schedule.period().endingAtInclusive().isBefore(todayDate);
    }

    private static boolean isScheduleStartingAfterDate(ScheduleDTO schedule, LocalDateTime today) {
        LocalDate todayDate = today.toLocalDate();
        return schedule.period().startingAt().isAfter(todayDate);
    }

    private boolean isScheduleOutsideTimeWindow(ScheduleDTO scheduleDTO, LocalDateTime today) {
        LocalDateTime minTime = today.minus(properties.intake().warnPeriod());
        LocalDateTime maxTime = today.plus(properties.intake().warnPeriod());
        LocalDateTime scheduleTime = LocalDateTime.of(today.toLocalDate(), scheduleDTO.time());
        return minTime.isAfter(scheduleTime) || maxTime.isBefore(scheduleTime);
    }

    private NotificationEntity createIntakeNotification(ScheduleDTO item) {
        return new NotificationEntity(
            item.userId(),
            NotificationType.INTAKE_EVENT,
            item.id(),
            "Time to take your medicine",
            "You have to take '" + item.medication().name() + "' at " + item.time(),
            Instant.now(clock).plus(properties.intake().lifetime())
        );
    }
}
