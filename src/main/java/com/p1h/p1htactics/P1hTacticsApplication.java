package com.p1h.p1htactics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P1hTacticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(P1hTacticsApplication.class, args);
    }

}
