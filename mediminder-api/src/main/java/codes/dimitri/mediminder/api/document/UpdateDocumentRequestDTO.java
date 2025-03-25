package codes.dimitri.mediminder.api.document;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateDocumentRequestDTO(
    LocalDate expiryDate,
    UUID relatedMedicationId,
    @Size(max = 128, message = "Description should not contain more than {max} characters")
    String description
) {
}
