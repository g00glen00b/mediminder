package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ScheduleEntityRepository extends JpaRepository<ScheduleEntity, UUID>, JpaSpecificationExecutor<ScheduleEntity> {
    Optional<ScheduleEntity> findByIdAndUserId(UUID id, String userId);

    @Query("""
        select distinct new codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO(s.userId, s.medicationId)
        from ScheduleEntity s
        where s.period.startingAt <= ?1
        and (s.period.endingAtInclusive is null or s.period.endingAtInclusive >= ?1)
    """)
    Page<UserScheduledMedicationDTO> findAllWithUserScheduledMedicationOnDate(LocalDate date, Pageable pageable);

    @Query("""
    select s from ScheduleEntity s
    where s.period.startingAt <= ?2
    and (s.period.endingAtInclusive is null or s.period.endingAtInclusive >= ?1)
    and s.medicationId = ?3
    """)
    List<ScheduleEntity> findAllByMedicationIdAndDateInPeriodGroup(LocalDate from, LocalDate until, UUID medicationId);

    @Modifying
    void deleteAllByMedicationId(UUID medicationId);

    @Query("""
    select s from ScheduleEntity s
    where s.period.startingAt <= ?2
    and (s.period.endingAtInclusive is null or s.period.endingAtInclusive >= ?1)
    """)
    Page<ScheduleEntity> findAllByOverlappingPeriod(LocalDate from, LocalDate untilInclusive, Pageable pageable);
}
