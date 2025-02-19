package com.gridgain.training.spring;

import org.apache.ignite.Ignite;
import org.apache.ignite.springframework.boot.autoconfigure.IgniteConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteConfig {
    @Bean(name = "igniteInstance")
    public Ignite igniteInstance(Ignite ignite) {
        return ignite;
    }
    @Bean
    public IgniteConfigurer configurer() {
        return igniteConfiguration -> {
            igniteConfiguration.setClientMode(true);
        };
    }
}