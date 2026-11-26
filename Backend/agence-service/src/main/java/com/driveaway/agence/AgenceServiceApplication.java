package com.driveaway.agence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AgenceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenceServiceApplication.class, args);
    }
}
