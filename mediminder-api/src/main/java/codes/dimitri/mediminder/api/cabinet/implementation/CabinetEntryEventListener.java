package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationDeletedEvent;
import codes.dimitri.mediminder.api.schedule.EventCompletedEvent;
import codes.dimitri.mediminder.api.schedule.EventUncompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CabinetEntryEventListener {
    private final CabinetEntryManager manager;

    @EventListener
    public void handleMedicationDeletedEvent(MedicationDeletedEvent event) {
        manager.deleteAllByMedicationId(event.id());
    }

    @EventListener
    public void handleEventCompletedEvent(EventCompletedEvent event) {
        manager.subtractDosesByMedicationId(event.medicationId(), event.dose());
    }

    @EventListener
    public void handleEventUncompletedEvent(EventUncompletedEvent event) {
        manager.addDosesByMedicationId(event.medicationId(), event.dose());
    }
}
