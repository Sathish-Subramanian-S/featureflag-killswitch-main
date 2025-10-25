package com.macys.stores.factory.parentapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"com.macys.stores"})
@ConfigurationPropertiesScan("com.macys.stores.factory.ldkillswitch.caching")
public class ParentAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParentAppApplication.class, args);
    }

}