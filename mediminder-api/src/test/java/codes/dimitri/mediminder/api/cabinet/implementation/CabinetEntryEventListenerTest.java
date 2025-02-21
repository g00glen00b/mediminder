package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationDeletedEvent;
import codes.dimitri.mediminder.api.schedule.EventCompletedEvent;
import codes.dimitri.mediminder.api.schedule.EventUncompletedEvent;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CabinetEntryEventListenerTest {
    @InjectMocks
    private CabinetEntryEventListener listener;
    @Mock
    private CabinetEntryManager manager;

    @Test
    void handleMedicationDeletedEvent() {
        // Given
        MedicationDeletedEvent event = Instancio.create(MedicationDeletedEvent.class);
        // When
        listener.handleMedicationDeletedEvent(event);
        // Then
        verify(manager).deleteAllByMedicationId(event.id());
    }

    @Test
    void handleEventCompletedEvent() {
        // Given
        EventCompletedEvent event = Instancio.create(EventCompletedEvent.class);
        // When
        listener.handleEventCompletedEvent(event);
        // Then
        verify(manager).subtractDosesByMedicationId(event.medicationId(), event.dose());
    }

    @Test
    void handleEventUncompletedEvent() {
        // Given
        EventUncompletedEvent event = Instancio.create(EventUncompletedEvent.class);
        // When
        listener.handleEventUncompletedEvent(event);
        // Then
        verify(manager).addDosesByMedicationId(event.medicationId(), event.dose());
    }
}