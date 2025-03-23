package codes.dimitri.mediminder.api.document.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface DocumentEntityRepository extends JpaRepository<DocumentEntity, UUID> {
    Optional<DocumentEntity> findByIdAndUserId(UUID id, UUID userId);
    Page<DocumentEntity> findAllByUserId(UUID userId, Pageable pageable);
}
