package codes.dimitri.mediminder.api.schedule.implementation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Period;

class PositiveIntervalValidator implements ConstraintValidator<PositiveInterval, Period> {
    @Override
    public boolean isValid(Period period, ConstraintValidatorContext constraintValidatorContext) {
        return period == null || (!period.isZero() && !period.isNegative());
    }
}
