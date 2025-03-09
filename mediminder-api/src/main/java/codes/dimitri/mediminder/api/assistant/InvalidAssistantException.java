package codes.dimitri.mediminder.api.assistant;

public class InvalidAssistantException extends RuntimeException {
    public InvalidAssistantException(String message) {
        super(message);
    }
}
