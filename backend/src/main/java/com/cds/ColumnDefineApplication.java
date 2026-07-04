package com.cds;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cds.mapper")
public class ColumnDefineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColumnDefineApplication.class, args);
    }
}
