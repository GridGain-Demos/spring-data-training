package com.gridgain.training.thinclient;

import org.apache.ignite.springframework.boot.autoconfigure.IgniteAutoConfiguration;
import org.apache.ignite.springframework.boot.autoconfigure.IgniteClientConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, IgniteAutoConfiguration.class})
public class ThinClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThinClientApplication.class);
    }

    @Bean
    IgniteClientConfigurer configurer() {
        return cfg -> {
            cfg.setAddresses("127.0.0.1:10800");
            cfg.setSendBufferSize(64*1024);
        };
    }
}