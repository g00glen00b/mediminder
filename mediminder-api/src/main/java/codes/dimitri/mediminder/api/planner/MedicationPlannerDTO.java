package codes.dimitri.mediminder.api.planner;

import codes.dimitri.mediminder.api.medication.MedicationDTO;

import java.math.BigDecimal;

public record MedicationPlannerDTO(MedicationDTO medication, BigDecimal availableDoses, BigDecimal requiredDoses) {
}
