package com.gridgain.training.thinclient;

import org.apache.ignite.springframework.boot.autoconfigure.IgniteAutoConfiguration;
import org.apache.ignite.springframework.boot.autoconfigure.IgniteClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
// import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.apache.ignite.springdata22.repository.config.EnableIgniteRepositories;
import org.apache.ignite.Ignite;

// @Configuration
@SpringBootApplication (exclude = {DataSourceAutoConfiguration.class, IgniteAutoConfiguration.class, IgniteClientAutoConfiguration.class})
@EnableIgniteRepositories (basePackages = "com.gridgain.training.spring")
@ComponentScan  (basePackages= "com.gridgain.training.spring")
public class ThinClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThinClientApplication.class);
    }

}