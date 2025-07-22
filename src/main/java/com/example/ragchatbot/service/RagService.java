package com.example.ragchatbot.service;

import com.example.ragchatbot.model.ChatResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * RAG 服务接口
 */
public interface RagService {


    @Async
    @Transactional
    void processDocument(byte[] fileContent, String originalFilename);

    /**
     * 回答用户问题
     */
    ChatResponse answerQuestion(String question);
}
