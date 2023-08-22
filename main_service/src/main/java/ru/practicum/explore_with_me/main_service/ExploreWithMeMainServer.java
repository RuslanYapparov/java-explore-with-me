package ru.practicum.explore_with_me.main_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "ru.practicum.explore_with_me.main_service",
        "ru.practicum.explore_with_me.stats_service"})
public class ExploreWithMeMainServer {

    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeMainServer.class);
    }

}