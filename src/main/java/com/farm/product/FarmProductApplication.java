package com.farm.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.farm.product.mapper")
public class FarmProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmProductApplication.class, args);
    }

}
