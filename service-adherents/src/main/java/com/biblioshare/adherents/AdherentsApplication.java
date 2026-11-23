package com.biblioshare.adherents;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication

public class AdherentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdherentsApplication.class, args);
    }
}
