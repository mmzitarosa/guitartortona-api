package it.mmzitarosa.guitartortona;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
abstract class DBTest {

	static MariaDBContainer<?> mariaDB = new MariaDBContainer<>(
			"mariadb:11"
	);

	@Autowired JdbcTemplate jdbcTemplate;
	private static boolean executed;

	@BeforeAll static void setup() throws IOException {
		log.info("BeforeAll");
		mariaDB.start();
	}

	@AfterAll static void afterAll() {
		log.info("AfterAll");
		mariaDB.stop();
	}

	@BeforeEach void before() throws IOException {
		log.info("Before");
		if (executed) return;
		String sql = Files.readString(Paths.get("src/test/resources/trigger.sql"));
		jdbcTemplate.execute(sql);
		executed = true;
	}

	@DynamicPropertySource static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
		registry.add("spring.datasource.username", mariaDB::getUsername);
		registry.add("spring.datasource.password", mariaDB::getPassword);
	}

}
