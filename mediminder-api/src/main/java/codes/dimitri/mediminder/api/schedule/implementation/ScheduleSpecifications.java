package codes.dimitri.mediminder.api.schedule.implementation;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

final class ScheduleSpecifications {
    private ScheduleSpecifications() {}

    public static Specification<ScheduleEntity> medicationId(UUID medicationId) {
        if (medicationId == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("medicationId"), medicationId);
    }

    public static Specification<ScheduleEntity> userId(String userId) {
        if (userId == null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<ScheduleEntity> onlyActive(boolean onlyActive, LocalDate currentDateForUser) {
        if (!onlyActive) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.lessThanOrEqualTo(root.get("period").get("startingAt"), currentDateForUser),
            criteriaBuilder.or(
                criteriaBuilder.greaterThanOrEqualTo(root.get("period").get("endingAtInclusive"), currentDateForUser),
                criteriaBuilder.isNull(root.get("period").get("endingAtInclusive"))
            )
        );
    }
}
