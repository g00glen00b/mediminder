package codes.dimitri.mediminder.api.medication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DoseTypeNotFoundException extends RuntimeException {
    private final String id;

    @Override
    public String getMessage() {
        return "Dose type with ID '" + id + "' does not exist";
    }
}
