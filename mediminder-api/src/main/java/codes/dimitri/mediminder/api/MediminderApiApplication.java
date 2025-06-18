package codes.dimitri.mediminder.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Slf4j
@EnableRetry
@EnableMethodSecurity
@SpringBootApplication
@ConfigurationPropertiesScan
public class MediminderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediminderApiApplication.class, args);
	}
}
