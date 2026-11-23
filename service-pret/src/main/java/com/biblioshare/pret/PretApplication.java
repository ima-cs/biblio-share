package com.biblioshare.pret;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableFeignClients
public class PretApplication {
    public static void main(String[] args) {
        SpringApplication.run(PretApplication.class, args);
    }
}
