package com.ruipeng.planner.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration
@Profile("!test")
public class DotenvConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure().load();
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(Dotenv dotenv) {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        dotenv.entries().forEach(entry -> properties.setProperty(entry.getKey(), entry.getValue()));
        configurer.setProperties(properties);
        configurer.setLocalOverride(true);
        return configurer;
    }
}