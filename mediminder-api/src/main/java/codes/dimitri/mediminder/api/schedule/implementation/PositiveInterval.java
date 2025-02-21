package codes.dimitri.mediminder.api.schedule.implementation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PositiveIntervalValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveInterval {
    String message() default "Invalid interval";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
