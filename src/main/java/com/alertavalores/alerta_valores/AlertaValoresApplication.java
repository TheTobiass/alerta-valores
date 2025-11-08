package com.alertavalores.alerta_valores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.alertavalores.alerta_valores", "controller", "service", "model", "repository"})
@EntityScan(basePackages = {"model"})
@EnableJpaRepositories(basePackages = {"repository"})
public class AlertaValoresApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertaValoresApplication.class, args);
	}

}
