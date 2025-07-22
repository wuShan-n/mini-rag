package com.example.ragchatbot.controller;

import com.example.ragchatbot.model.ChatRequest;
import com.example.ragchatbot.model.ChatResponse;
import com.example.ragchatbot.model.UploadResponse;
import com.example.ragchatbot.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Slf4j
public class ChatController {

    @Autowired
    private RagService ragService;

    /**
     * 文档上传端点
     */
    @PostMapping("/documents")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("上传的文件不能为空");
        }

        try {
            // 【核心修改】在同步方法内，立即将文件内容读入字节数组
            byte[] fileContent = file.getBytes();
            String originalFilename = file.getOriginalFilename();

            // 将字节数组和文件名传递给异步服务
            ragService.processDocument(fileContent, originalFilename);

            // 立即返回“已接受”响应
            String message = String.format("文件 '%s' 上传成功，正在后台进行处理...", originalFilename);
            return new ResponseEntity<>(Map.of("message", message), HttpStatus.ACCEPTED);

        } catch (IOException e) {
            log.error("读取上传文件失败: {}", e.getMessage());
            return new ResponseEntity<>(Map.of("message", "读取文件内容时出错"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 聊天问答端点
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = ragService.answerQuestion(request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ChatResponse.error("问答处理失败: " + e.getMessage()));
        }
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RAG 智能问答机器人服务运行正常");
    }
}
