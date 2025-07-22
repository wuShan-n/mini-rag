package com.example.ragchatbot.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private String answer;
    private List<String> sourceChunks;
    private boolean success;

    public static ChatResponse success(String answer, List<String> sourceChunks) {
        ChatResponse response = new ChatResponse();
        response.setAnswer(answer);
        response.setSourceChunks(sourceChunks);
        response.setSuccess(true);
        return response;
    }

    public static ChatResponse error(String message) {
        ChatResponse response = new ChatResponse();
        response.setAnswer(message);
        response.setSuccess(false);
        return response;
    }
}
