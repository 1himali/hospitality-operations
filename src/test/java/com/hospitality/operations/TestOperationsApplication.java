package com.hospitality.operations;

import org.springframework.boot.SpringApplication;

public class TestOperationsApplication {

	public static void main(String[] args) {
		SpringApplication.from(OperationsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
