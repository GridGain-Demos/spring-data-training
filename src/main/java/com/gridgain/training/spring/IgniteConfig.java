package com.gridgain.training.spring;

import org.apache.ignite.client.IgniteClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.springdata22.repository.config.EnableIgniteRepositories;

@Configuration
@EnableIgniteRepositories(basePackages = "com.gridgain.training.spring")
public class IgniteConfig {
@Bean(name = "igniteInstance")
public IgniteClient igniteClient() {
    // Client configuration
    ClientConfiguration clientCfg = new ClientConfiguration();

    // Set the addresses of one or more Ignite nodes in the cluster.
    // You can specify multiple addresses for failover.
    clientCfg.setAddresses("127.0.0.1:10800", "127.0.0.1:10801");

    // Start Ignite client
    IgniteClient igniteClient = Ignition.startClient(clientCfg);

    return igniteClient;
}
}