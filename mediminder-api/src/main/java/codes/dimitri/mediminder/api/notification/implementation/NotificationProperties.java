package codes.dimitri.mediminder.api.notification.implementation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.time.Period;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
    // Can be generated through https://www.attheminute.com/vapid-key-generator
    String publicKey,
    String privateKey,
    @DefaultValue("50") int chunkSize,
    @DefaultValue ExpiryProperties expiry,
    @DefaultValue DoseProperties dose,
    @DefaultValue IntakeProperties intake
    ) {

    public record ExpiryProperties(
        @DefaultValue("1w") Period lifetime,
        @DefaultValue("1w") Period warnPeriod) { }

    public record DoseProperties(
        @DefaultValue("1w") Period lifetime,
        @DefaultValue("1w") Period warnPeriod) { }

    public record IntakeProperties(
        @DefaultValue("2h") Duration lifetime,
        @DefaultValue("1d") Period bufferWindow,
        @DefaultValue("1h") Duration warnPeriod) {
    }
}
