package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDeletedEvent;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleEventListenerTest {
    @InjectMocks
    private ScheduleEventListener listener;
    @Mock
    private ScheduleManager manager;

    @Test
    void handleMedicationDeletedEvent() {
        // Given
        var event = Instancio.create(MedicationDeletedEvent.class);
        // When
        listener.handleMedicationDeletedEvent(event);
        // Then
        verify(manager).deleteAllByMedicationId(event.id());
    }
}