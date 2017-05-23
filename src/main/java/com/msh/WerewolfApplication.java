package com.msh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;

@SpringBootApplication
@MapperScan(basePackages = "com.msh.common.mapper")
public class WerewolfApplication {
    public static void main(String[] args) {
        SpringApplication.run(WerewolfApplication.class, args);
    }
}
