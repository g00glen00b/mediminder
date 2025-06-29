package codes.dimitri.mediminder.api.medication.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface MedicationEntityRepository extends JpaRepository<MedicationEntity, UUID> {
    Optional<MedicationEntity> findByIdAndUserId(UUID id, String userId);
    Page<MedicationEntity> findAllByUserId(String userId, Pageable pageable);
    Page<MedicationEntity> findAllByUserIdAndNameContainingIgnoreCase(String userId, String search, Pageable pageable);
    List<MedicationEntity> findAllByUserId(String userId);
}
