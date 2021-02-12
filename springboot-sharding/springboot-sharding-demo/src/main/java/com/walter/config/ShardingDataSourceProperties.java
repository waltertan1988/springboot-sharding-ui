package com.walter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("shardingsphere.datasource")
public class ShardingDataSourceProperties {
    private String name;
    private String driverClassName;
    private List<String> hosts;
    private String urlPattern;
    private String username;
    private String password;
    private String[] entityPackagesToScan;
    private int tableCount;
}
