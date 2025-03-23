package codes.dimitri.mediminder.api.document;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DocumentNotFoundException extends RuntimeException {
    private final UUID id;

    @Override
    public String getMessage() {
        return "Document with ID '" + id + "' does not exist";
    }
}
