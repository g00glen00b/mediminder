package codes.dimitri.mediminder.api.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Optional;

public class ValidationUtilities {
    public static Optional<ConstraintViolation<?>> getAnyConstraintViolation(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream().findAny();
    }
}
