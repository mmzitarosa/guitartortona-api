package it.mmzitarosa.guitartortona;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GuitarTortonaApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuitarTortonaApplication.class, args);
	}

}
