package codes.dimitri.mediminder.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MediminderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediminderApiApplication.class, args);
	}
}
