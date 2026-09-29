package devMario.example.kioscoLaMadrina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KioscoLaMadrinaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KioscoLaMadrinaApplication.class, args);
	}

}
