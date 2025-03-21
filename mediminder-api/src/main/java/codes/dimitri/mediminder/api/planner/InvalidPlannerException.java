package codes.dimitri.mediminder.api.planner;

public class InvalidPlannerException extends RuntimeException {
    public InvalidPlannerException(String message) {
        super(message);
    }

    public InvalidPlannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
