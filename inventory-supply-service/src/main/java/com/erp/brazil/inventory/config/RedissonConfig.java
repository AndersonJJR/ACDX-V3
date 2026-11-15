package com.erp.brazil.inventory.config;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer() {
        return config -> {
            SingleServerConfig serverConfig = config.useSingleServer()
                    .setAddress("redis://" + redisHost + ":" + redisPort)
                    .setDatabase(redisDatabase)
                    .setConnectionPoolSize(50)
                    .setConnectionMinimumIdleSize(10)
                    .setIdleConnectionTimeout(10000)
                    .setConnectTimeout(10000)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500)
                    .setPingConnectionInterval(30000)
                    .setKeepAlive(true);

            if (!redisPassword.isEmpty()) {
                serverConfig.setPassword(redisPassword);
            }
        };
    }

    @Bean
    public Config redissonConfig() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setDatabase(redisDatabase)
                .setConnectionPoolSize(50)
                .setConnectionMinimumIdleSize(10)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setPingConnectionInterval(30000)
                .setKeepAlive(true);

        if (!redisPassword.isEmpty()) {
            serverConfig.setPassword(redisPassword);
        }

        return config;
    }
}