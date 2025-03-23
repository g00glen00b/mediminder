package codes.dimitri.mediminder.api.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentManager {
    @Transactional
    DocumentDTO createForCurrentUser(@Valid @NotNull CreateDocumentRequestDTO request, @NotNull MultipartFile file);
    @Transactional
    DocumentDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateDocumentRequestDTO request);
    Page<DocumentDTO> findAllForCurrentUser(@NotNull Pageable pageable);
    DocumentDTO findByIdForCurrentUser(@NotNull UUID id);
    ResourceWithMetadataDTO downloadDocumentForCurrentUser(@NotNull UUID id);
    @Transactional
    void deleteForCurrentUser(@NotNull UUID id);
}
