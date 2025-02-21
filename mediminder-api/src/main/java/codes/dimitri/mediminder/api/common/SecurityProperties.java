package codes.dimitri.mediminder.api.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(@DefaultValue("http://localhost:4200") String allowedOrigin) {
}
