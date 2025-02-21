package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDeletedEvent;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ScheduleEventListener {
    private final ScheduleManager manager;

    @EventListener
    public void handleMedicationDeletedEvent(MedicationDeletedEvent event) {
        manager.deleteAllByMedicationId(event.id());
    }
}
