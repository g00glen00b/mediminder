package codes.dimitri.mediminder.api.notification.implementation;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.Security;

@Configuration
class PushNotificationConfiguration {
    @Bean
    public PushService pushService(NotificationProperties properties) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        return new PushService(properties.publicKey(), properties.privateKey());
    }
}
