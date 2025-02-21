package codes.dimitri.mediminder.api.user;

public class UserMailFailedException extends RuntimeException {
    public UserMailFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
