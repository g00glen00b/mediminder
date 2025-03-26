package codes.dimitri.mediminder.api.cabinet.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CabinetEntryEntityRepository extends JpaRepository<CabinetEntryEntity, UUID> {
    Page<CabinetEntryEntity> findAllByUserId(UUID userId, Pageable pageable);
    Page<CabinetEntryEntity> findAllByMedicationIdAndUserId(UUID medicationId, UUID userId, Pageable pageable);
    Optional<CabinetEntryEntity> findByIdAndUserId(UUID id, UUID userId);
    @Query("select e from CabinetEntryEntity e where e.remainingDoses > 0 and e.expiryDate <= ?1")
    Page<CabinetEntryEntity> findAllWithRemainingDosesWithExpiryDateBefore(LocalDate expiryDate, Pageable pageable);

    @Query("""
        select sum(e.remainingDoses)
        from CabinetEntryEntity e
        where e.medicationId = ?1
    """)
    BigDecimal sumRemainingDosesByMedicationId(UUID medicationId);

    @Modifying
    void deleteAllByMedicationId(UUID medicationId);

    @Query("select e from CabinetEntryEntity e where e.remainingDoses > 0 and e.medicationId = ?1")
    Page<CabinetEntryEntity> findAllWithRemainingDosesByMedicationId(UUID medicationId, Pageable pageable);

    @Query("select e from CabinetEntryEntity e where e.medicationId = ?1")
    Page<CabinetEntryEntity> findAllByMedicationId(UUID medicationId, Pageable pageable);
}
