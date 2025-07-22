package com.example.ragchatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${api.huggingface.url}")
    private String huggingFaceUrl;



    @Bean(name = "huggingFaceWebClient")
    public WebClient huggingFaceWebClient() {
        return WebClient.builder()
                .baseUrl(huggingFaceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "chatWebClient")
    public WebClient chatWebClient(@Value("${api.chat.url}") String chatUrl,
            @Value("${api.chat.token}") String chatToken) {
        return WebClient.builder()
                .baseUrl(chatUrl)
                .defaultHeader("Authorization", "Bearer " + chatToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
