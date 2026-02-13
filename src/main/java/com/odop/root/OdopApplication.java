package com.odop.root;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableMongoRepositories(basePackages = {"com.odop", "com.exhaustedpigeon"})
@EnableScheduling
//@EnableEurekaServer
@ComponentScan(basePackages = {"com.odop", "com.exhaustedpigeon"})
public class OdopApplication {
	public static void main(String[] args) {
		SpringApplication.run(OdopApplication.class, args);
	}
}
