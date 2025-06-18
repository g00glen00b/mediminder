package codes.dimitri.mediminder.api.document.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

interface DocumentEntityRepository extends JpaRepository<DocumentEntity, UUID>, JpaSpecificationExecutor<DocumentEntity> {
    Optional<DocumentEntity> findByIdAndUserId(UUID id, String userId);
    Page<DocumentEntity> findAllByExpiryDateLessThanEqual(LocalDate expiryDate, Pageable pageable);
}
