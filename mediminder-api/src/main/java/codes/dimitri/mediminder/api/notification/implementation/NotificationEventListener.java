package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.NotificationManager;
import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.schedule.EventCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationManager notificationManager;

    @EventListener
    public void handleIntakeCompletion(EventCompletedEvent event) {
        notificationManager.deleteAllByUserIdTypeAndInitiatorId(
            event.userId(),
            NotificationType.INTAKE_EVENT,
            event.scheduleId()
        );
    }
}
