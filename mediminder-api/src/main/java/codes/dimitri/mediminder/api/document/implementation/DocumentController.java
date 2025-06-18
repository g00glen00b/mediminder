package codes.dimitri.mediminder.api.document.implementation;

import codes.dimitri.mediminder.api.document.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/document")
@PreAuthorize("hasAuthority('Document')")
@RequiredArgsConstructor
class DocumentController {
    private final DocumentManager manager;

    @GetMapping
    public Page<DocumentDTO> findAll(
        @RequestParam(required = false) LocalDate expiredOn,
        @RequestParam(required = false) UUID medicationId,
        @ParameterObject Pageable pageable) {
        return manager.findAllForCurrentUser(expiredOn, medicationId, pageable);
    }

    @GetMapping("/{id}")
    public DocumentDTO findById(@PathVariable UUID id) {
        return manager.findByIdForCurrentUser(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        ResourceWithMetadataDTO resource = manager.downloadDocumentForCurrentUser(id);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + resource.filename() + "\"")
            .header("Content-Type", resource.contentType())
            .body(resource.resource());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
        mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
        encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public DocumentDTO create(
        @RequestPart("file") MultipartFile file,
        @RequestPart("request") CreateDocumentRequestDTO request) {
        return manager.createForCurrentUser(request, file);
    }

    @PutMapping("/{id}")
    public DocumentDTO update(
        @PathVariable UUID id,
        @RequestBody UpdateDocumentRequestDTO request) {
        return manager.updateForCurrentUser(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        manager.deleteForCurrentUser(id);
    }

    @ExceptionHandler(InvalidDocumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidDocumentException(InvalidDocumentException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid document")
            .type(URI.create("https://mediminder/document/invalid"))
            .build();
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDocumentNotFoundException(DocumentNotFoundException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
            .title("Document not found")
            .type(URI.create("https://mediminder/document/notfound"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid document")
            .type(URI.create("https://mediminder/document/invalid"))
            .build();
    }
}
