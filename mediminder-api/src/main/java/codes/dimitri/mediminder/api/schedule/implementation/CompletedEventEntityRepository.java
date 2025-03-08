package codes.dimitri.mediminder.api.schedule.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CompletedEventEntityRepository extends JpaRepository<CompletedEventEntity, UUID> {
    @Query("select e from CompletedEventEntity e where e.userId = ?1 and e.targetDate >= ?2 and e.targetDate < ?3")
    List<CompletedEventEntity> findByUserIdAndTargetDate(UUID userId, LocalDateTime targetDateStart, LocalDateTime targetDateEnd);
    Optional<CompletedEventEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByScheduleIdAndTargetDate(UUID scheduleId, LocalDateTime targetDate);
}
