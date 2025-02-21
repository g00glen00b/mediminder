package codes.dimitri.mediminder.api.medication;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdministrationTypeNotFoundException extends RuntimeException {
    private final String id;

    @Override
    public String getMessage() {
        return "Administration type with ID '" + id + "' does not exist";
    }
}
