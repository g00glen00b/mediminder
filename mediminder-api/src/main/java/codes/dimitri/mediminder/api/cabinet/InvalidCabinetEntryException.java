package codes.dimitri.mediminder.api.cabinet;

public class InvalidCabinetEntryException extends RuntimeException {
    public InvalidCabinetEntryException(String message) {
        super(message);
    }
}
