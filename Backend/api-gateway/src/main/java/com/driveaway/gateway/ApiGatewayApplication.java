package com.driveaway.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

@SpringBootApplication
public class ApiGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public CommandLineRunner printStartupInfo(Environment env) {
        return args -> {
            String port = env.getProperty("server.port", "8080");
            String eurekaUrl = env.getProperty("eureka.client.service-url.defaultZone", "http://localhost:8761/eureka/");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();

            log.info("====================================================================");
            log.info("🚀 DriveAway Central API Gateway is RUNNING!");
            log.info("🌐 Local Gateway URL:      http://localhost:{}", port);
            log.info("🌐 Network Gateway URL:    http://{}:{}", hostAddress, port);
            log.info("📡 Eureka Registry Target: {}", eurekaUrl);
            log.info("🔍 Live Route Monitor:     http://localhost:{}/actuator/gateway/routes", port);
            log.info("🩺 Health Check Endpoint:  http://localhost:{}/actuator/health", port);
            log.info("====================================================================");
        };
    }
}
