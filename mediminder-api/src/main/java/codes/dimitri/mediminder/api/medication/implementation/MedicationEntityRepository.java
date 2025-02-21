package codes.dimitri.mediminder.api.medication.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface MedicationEntityRepository extends JpaRepository<MedicationEntity, UUID> {
    Optional<MedicationEntity> findByIdAndUserId(UUID id, UUID userId);
    Page<MedicationEntity> findAllByUserId(UUID userId, Pageable pageable);
    Page<MedicationEntity> findAllByUserIdAndNameContainingIgnoreCase(UUID userId, String search, Pageable pageable);
}
