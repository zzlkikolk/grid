package com.jerryz.grid;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.jerryz.grid.mapper")
public class GridApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridApplication.class, args);
    }

}
