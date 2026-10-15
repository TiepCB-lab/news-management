package com.uth.newsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NewsManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsManagementApplication.class, args);
    }
}
