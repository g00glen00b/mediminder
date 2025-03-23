package codes.dimitri.mediminder.api.document;

import codes.dimitri.mediminder.api.medication.MedicationDTO;

import java.time.LocalDate;
import java.util.UUID;

public record DocumentDTO(
    UUID id,
    UUID userId,
    String filename,
    LocalDate expiryDate,
    MedicationDTO relatedMedication,
    String description
) {
}
