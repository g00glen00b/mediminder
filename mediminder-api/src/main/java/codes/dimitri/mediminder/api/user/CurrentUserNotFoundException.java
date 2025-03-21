package codes.dimitri.mediminder.api.user;

public class CurrentUserNotFoundException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Current user not found";
    }
}
