package codes.dimitri.mediminder.api.document.implementation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class DocumentEntity {
    @Id
    private UUID id;
    private String userId;
    private String filename;
    private String contentType;
    private LocalDate expiryDate;
    private UUID relatedMedicationId;
    private String description;

    public DocumentEntity(String userId, String filename, String contentType, LocalDate expiryDate, UUID relatedMedicationId, String description) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.filename = filename;
        this.contentType = contentType;
        this.expiryDate = expiryDate;
        this.relatedMedicationId = relatedMedicationId;
        this.description = description;
    }
}
