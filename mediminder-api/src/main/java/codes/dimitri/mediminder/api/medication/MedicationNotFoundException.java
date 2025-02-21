package codes.dimitri.mediminder.api.medication;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MedicationNotFoundException extends RuntimeException {
    private final UUID id;

    @Override
    public String getMessage() {
        return "Medication with ID '" + id + "' does not exist";
    }
}
