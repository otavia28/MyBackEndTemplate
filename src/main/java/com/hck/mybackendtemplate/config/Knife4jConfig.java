package com.hck.mybackendtemplate.config;

import io.swagger.models.auth.In;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.List;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {
    @Bean
    public Docket dockerBean() {
        // 指定使用 Swagger2 规范
        return new Docket(DocumentationType.SWAGGER_2)
                // 设置 API 文档的基本信息
                .apiInfo(new ApiInfoBuilder()
                        .title("MyBackEndTemplate-API")
                        .description("MyBackEndTemplate 项目的 api 文档")
                        .contact("otavia28")
                        .version("1.0")
                        .build())
                .select()
                // 设置 Controller 扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.hck.mybackendtemplate.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
