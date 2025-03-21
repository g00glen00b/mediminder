package codes.dimitri.mediminder.api.schedule;

public class InvalidScheduleException extends RuntimeException {
    public InvalidScheduleException(String message) {
        super(message);
    }

    public InvalidScheduleException(String message, Throwable cause) {
        super(message, cause);
    }
}
