package codes.dimitri.mediminder.api.cabinet;

public class InvalidCabinetEntryException extends RuntimeException {
    public InvalidCabinetEntryException(String message) {
        super(message);
    }

    public InvalidCabinetEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}
