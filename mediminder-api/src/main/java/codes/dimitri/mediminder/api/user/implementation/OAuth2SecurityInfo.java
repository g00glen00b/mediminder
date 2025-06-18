package codes.dimitri.mediminder.api.user.implementation;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record OAuth2SecurityInfo(String userId, Authentication authentication, OAuth2User oAuth2User) {
}
