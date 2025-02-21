package codes.dimitri.mediminder.api.medication;

public class InvalidMedicationException extends RuntimeException {
    public InvalidMedicationException(String message) {
        super(message);
    }
}
