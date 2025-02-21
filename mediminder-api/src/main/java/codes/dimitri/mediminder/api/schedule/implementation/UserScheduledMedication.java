package codes.dimitri.mediminder.api.schedule.implementation;

import java.util.UUID;

interface UserScheduledMedication {
    UUID getUserId();
    UUID getMedicationId();
}
