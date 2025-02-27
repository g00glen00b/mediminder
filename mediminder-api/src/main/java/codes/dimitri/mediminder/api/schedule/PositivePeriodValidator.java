package codes.dimitri.mediminder.api.schedule;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PositivePeriodValidator implements ConstraintValidator<PositiveSchedulePeriod, SchedulePeriodDTO> {

    @Override
    public boolean isValid(SchedulePeriodDTO schedulePeriodDTO, ConstraintValidatorContext constraintValidatorContext) {
        if (schedulePeriodDTO.endingAtInclusive() == null || schedulePeriodDTO.startingAt() == null) return true;
        return schedulePeriodDTO.endingAtInclusive().isAfter(schedulePeriodDTO.startingAt());
    }
}
