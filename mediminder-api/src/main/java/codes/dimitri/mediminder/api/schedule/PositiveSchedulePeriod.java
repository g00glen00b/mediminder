package codes.dimitri.mediminder.api.schedule;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PositivePeriodValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveSchedulePeriod {
    String message() default "Invalid interval";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
