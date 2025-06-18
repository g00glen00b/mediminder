package codes.dimitri.mediminder.api.user.implementation;

public record UserEntitySecurityInfo(UserEntity entity, OAuth2SecurityInfo securityInfo) {
}
