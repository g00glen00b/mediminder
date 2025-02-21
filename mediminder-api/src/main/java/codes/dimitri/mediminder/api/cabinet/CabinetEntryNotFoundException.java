package codes.dimitri.mediminder.api.cabinet;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class CabinetEntryNotFoundException extends RuntimeException {
    private final UUID id;

    @Override
    public String getMessage() {
        return "Cabinet entry with ID '" + id + "' does not exist";
    }
}
