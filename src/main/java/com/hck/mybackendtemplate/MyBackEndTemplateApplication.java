package com.hck.mybackendtemplate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hck.mybackendtemplate.Mapper")
public class MyBackEndTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBackEndTemplateApplication.class, args);
    }

}
