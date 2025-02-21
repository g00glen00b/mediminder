package codes.dimitri.mediminder.api.schedule;

import java.util.UUID;

public record UserScheduledMedicationDTO(UUID userId, UUID medicationId) {
}
