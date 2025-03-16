package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static codes.dimitri.mediminder.api.common.ValidationUtilities.getAnyConstraintViolation;

@RestController
@RequestMapping("/api/cabinet")
@RequiredArgsConstructor
class CabinetEntryController {
    private final CabinetEntryManager manager;

    @GetMapping
    public Page<CabinetEntryDTO> findAll(@ParameterObject Pageable pageable) {
        return manager.findAllForCurrentUser(pageable);
    }

    @GetMapping("/{id}")
    public CabinetEntryDTO findById(@PathVariable UUID id) {
        return manager.findByIdForCurrentUser(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CabinetEntryDTO create(@RequestBody CreateCabinetEntryRequestDTO request) {
        return manager.createForCurrentUser(request);
    }

    @PutMapping("/{id}")
    public CabinetEntryDTO update(@PathVariable UUID id, @RequestBody UpdateCabinetEntryRequestDTO request) {
        return manager.updateForCurrentUser(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        manager.deleteForCurrentUser(id);
    }

    @ExceptionHandler(CabinetEntryNotFoundException.class)
    public ErrorResponse handleNotFound(CabinetEntryNotFoundException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
            .title("Cabinet entry not found")
            .type(URI.create("https://mediminder/cabinet/notfound"))
            .build();
    }

    @ExceptionHandler(InvalidCabinetEntryException.class)
    public ErrorResponse handleInvalid(InvalidCabinetEntryException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid cabinet entry")
            .type(URI.create("https://mediminder/cabinet/invalid"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String detail = getAnyConstraintViolation(ex)
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, detail)
            .title("Invalid cabinet entry")
            .type(URI.create("https://mediminder/cabinet/invalid"))
            .build();
    }
}
