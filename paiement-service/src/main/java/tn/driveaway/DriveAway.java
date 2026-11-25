package tn.driveaway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DriveAway {
    public static void main(String[] args) {
        SpringApplication.run(DriveAway.class, args);
    }
}
