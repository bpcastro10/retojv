package com.proyecto.microcuentas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicrocuentasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrocuentasApplication.class, args);
	}

}
