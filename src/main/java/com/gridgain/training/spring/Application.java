package com.gridgain.training.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication // (scanBasePackages = "com.gridgain.training.spring", exclude = {IgniteClientAutoConfiguration.class})
@EnableJdbcRepositories
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
