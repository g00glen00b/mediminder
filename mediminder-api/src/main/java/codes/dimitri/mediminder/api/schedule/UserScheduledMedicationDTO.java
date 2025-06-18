package codes.dimitri.mediminder.api.schedule;

import java.util.UUID;

public record UserScheduledMedicationDTO(String userId, UUID medicationId) {
}
