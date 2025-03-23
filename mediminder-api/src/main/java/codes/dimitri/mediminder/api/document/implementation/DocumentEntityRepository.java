package codes.dimitri.mediminder.api.document.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

interface DocumentEntityRepository extends JpaRepository<DocumentEntity, UUID> {
    Optional<DocumentEntity> findByIdAndUserId(UUID id, UUID userId);
    Page<DocumentEntity> findAllByUserId(UUID userId, Pageable pageable);
    Page<DocumentEntity> findAllByUserIdAndExpiryDateLessThanEqual(UUID userId, LocalDate expiryDate, Pageable pageable);
    Page<DocumentEntity> findAllByExpiryDateLessThanEqual(LocalDate expiryDate, Pageable pageable);
}
