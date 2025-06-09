package com.ruipeng.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonalFinancialPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalFinancialPlannerApplication.class, args);
	}

}
