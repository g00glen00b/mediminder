package codes.dimitri.mediminder.api.document;

import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;

public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException(String message) {
        super(message);
    }

    public InvalidDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
