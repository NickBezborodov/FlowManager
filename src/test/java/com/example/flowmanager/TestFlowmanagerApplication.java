package com.example.flowmanager;

import org.springframework.boot.SpringApplication;

public class TestFlowmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.from(FlowmanagerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
