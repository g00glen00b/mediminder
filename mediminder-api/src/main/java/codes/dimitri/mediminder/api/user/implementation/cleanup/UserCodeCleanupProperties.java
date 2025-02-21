package codes.dimitri.mediminder.api.user.implementation.cleanup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Period;

@ConfigurationProperties(prefix = "user.batch.cleanup")
public record UserCodeCleanupProperties(
    @DefaultValue("1d") Period gracePeriod,
    @DefaultValue("50") int chunkSize,
    @DefaultValue("@daily") String schedule) {
}
