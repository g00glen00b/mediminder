package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.user.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MedicationEventListener {
    private final MedicationManager manager;

    @EventListener
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        manager.deleteAllByUserId(event.id());
    }
}
