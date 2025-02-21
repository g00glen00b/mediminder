package codes.dimitri.mediminder.api.user.implementation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "user")
public record UserProperties(
    String verificationUrl,
    String passwordResetUrl,
    @DefaultValue("noreply@mediminder.org") String noreplyAddress
) {
}
