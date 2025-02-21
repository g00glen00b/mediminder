package codes.dimitri.mediminder.api.medication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MedicationTypeNotFoundException extends RuntimeException {
    private final String id;

    @Override
    public String getMessage() {
        return "Medication type with ID '" + id + "' does not exist";
    }
}
