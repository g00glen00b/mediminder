package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static codes.dimitri.mediminder.api.common.ValidationUtilities.getAnyConstraintViolation;

@RestController
@RequestMapping("/api/medication")
@RequiredArgsConstructor
class MedicationController {
    private final MedicationManager manager;

    @GetMapping
    public Page<MedicationDTO> findAll(
        @RequestParam(required = false) String search,
        @ParameterObject Pageable pageable) {
        return manager.findAllForCurrentUser(search, pageable);
    }

    @GetMapping("/{id}")
    public MedicationDTO findById(@PathVariable UUID id) {
        return manager.findByIdForCurrentUser(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MedicationDTO create(@RequestBody CreateMedicationRequestDTO request) {
        return manager.createForCurrentUser(request);
    }

    @PutMapping("/{id}")
    public MedicationDTO update(@PathVariable UUID id, @RequestBody UpdateMedicationRequestDTO request) {
        return manager.updateForCurrentUser(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        manager.deleteByIdForCurrentUser(id);
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    public ErrorResponse handleNotFound(MedicationNotFoundException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
            .title("Medication not found")
            .type(URI.create("https://mediminder/medication/notfound"))
            .build();
    }

    @ExceptionHandler({
        InvalidMedicationException.class,
        MedicationTypeNotFoundException.class,
        AdministrationTypeNotFoundException.class,
        DoseTypeNotFoundException.class
    })
    public ErrorResponse handleInvalid(RuntimeException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid medication")
            .type(URI.create("https://mediminder/medication/invalid"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String detail = getAnyConstraintViolation(ex)
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, detail)
            .title("Invalid medication")
            .type(URI.create("https://mediminder/medication/invalid"))
            .build();
    }
}
