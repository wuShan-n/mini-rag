package com.example.ragchatbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.ragchatbot.mapper")
@EnableAsync
public class RagChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagChatbotApplication.class, args);
    }
}
