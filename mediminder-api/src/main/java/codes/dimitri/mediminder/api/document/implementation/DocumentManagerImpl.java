package codes.dimitri.mediminder.api.document.implementation;

import codes.dimitri.mediminder.api.document.*;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.medication.MedicationNotFoundException;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DocumentManagerImpl implements DocumentManager {
    private final DocumentProperties properties;
    private final DocumentEntityRepository repository;
    private final UserManager userManager;
    private final MedicationManager medicationManager;
    private final DocumentStorageService storageService;
    private final DocumentMapper mapper;

    @Override
    @Transactional
    public DocumentDTO createForCurrentUser(@Valid @NotNull CreateDocumentRequestDTO request, @NotNull MultipartFile file) {
        validateFilenameLength(file);
        validateFileType(file);
        UserDTO user = findCurrentUser();
        MedicationDTO medication = findMedication(request.relatedMedicationId(), user.id());
        DocumentEntity entity = new DocumentEntity(
            user.id(),
            file.getOriginalFilename(),
            file.getContentType(),
            request.expiresAt(),
            request.relatedMedicationId(),
            request.description()
        );
        storageService.upload(entity, file);
        DocumentEntity savedEntity = repository.save(entity);
        return mapper.toDTO(savedEntity, medication);
    }

    @Override
    @Transactional
    public DocumentDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateDocumentRequestDTO request) {
        UserDTO user = findCurrentUser();
        DocumentEntity entity = findEntity(id, user);
        MedicationDTO medication = findMedication(request.relatedMedicationId(), entity.getUserId());
        entity.setExpiryDate(request.expiresAt());
        entity.setRelatedMedicationId(request.relatedMedicationId());
        entity.setDescription(request.description());
        DocumentEntity savedEntity = repository.save(entity);
        return mapper.toDTO(savedEntity, medication);
    }

    @Override
    public Page<DocumentDTO> findAllForCurrentUser(@NotNull Pageable pageable) {
        return repository
            .findAllByUserId(findCurrentUser().id(), pageable)
            .map(entity -> mapper.toDTO(entity, findMedicationSafe(entity.getRelatedMedicationId(), entity.getUserId())));
    }

    @Override
    public DocumentDTO findByIdForCurrentUser(@NotNull UUID id) {
        UserDTO user = findCurrentUser();
        DocumentEntity entity = findEntity(id, user);
        MedicationDTO medication = findMedicationSafe(entity.getRelatedMedicationId(), entity.getUserId());
        return mapper.toDTO(entity, medication);
    }

    @Override
    public ResourceWithMetadataDTO downloadDocumentForCurrentUser(@NotNull UUID id) {
        UserDTO user = findCurrentUser();
        DocumentEntity entity = findEntity(id, user);
        return new ResourceWithMetadataDTO(
            storageService.download(entity),
            entity.getFilename(),
            entity.getContentType());
    }

    @Override
    @Transactional
    public void deleteForCurrentUser(@NotNull UUID id) {
        DocumentEntity entity = findEntity(id, findCurrentUser());
        storageService.delete(entity);
        repository.delete(entity);
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidDocumentException("User is not authenticated", ex);
        }
    }

    private MedicationDTO findMedication(UUID id, UUID userId) {
        if (id == null) return null;
        try {
            return medicationManager.findByIdAndUserId(id, userId);
        } catch (MedicationNotFoundException ex) {
            throw new InvalidDocumentException("Medication not found", ex);
        }
    }

    private MedicationDTO findMedicationSafe(UUID id, UUID userId) {
        try {
            return findMedication(id, userId);
        } catch (InvalidDocumentException ex) {
            return null;
        }
    }

    private DocumentEntity findEntity(UUID id, UserDTO currentUser) {
        return repository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !properties.allowedContentTypes().contains(contentType)) {
            throw new InvalidDocumentException("Files with content type '" + contentType + "' are not allowed");
        }
    }

    private void validateFilenameLength(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new InvalidDocumentException("Filename is empty");
        if (filename.length() > 128) throw new InvalidDocumentException("Filename '" + filename + "' cannot contain more than 128 characters");
    }
}
