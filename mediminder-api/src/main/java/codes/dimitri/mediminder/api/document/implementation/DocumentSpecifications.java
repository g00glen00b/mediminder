package codes.dimitri.mediminder.api.document.implementation;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

final class DocumentSpecifications {
    private DocumentSpecifications() {}

    public static Specification<DocumentEntity> userId(String userId) {
        if (userId == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<DocumentEntity> relatedMedicationId(UUID medicationId) {
        if (medicationId == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("relatedMedicationId"), medicationId);
    }

    public static Specification<DocumentEntity> expiryDateLessThanOrEqualTo(LocalDate expiryDate) {
        if (expiryDate == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("expiryDate"), expiryDate);
    }
}
