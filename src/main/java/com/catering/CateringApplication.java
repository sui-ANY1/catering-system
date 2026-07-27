package com.catering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.catering.mapper")
public class CateringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CateringApplication.class, args);
    }
}