package com.example.ragchatbot.model;

import com.pgvector.PGvector;
import lombok.Data;
import lombok.NoArgsConstructor;

// 【核心修改】移除了所有 jakarta.persistence 和 org.hibernate 的注解
@Data
@NoArgsConstructor
public class DocumentChunk {

    private Long id;
    private String content;
    private String sourceDocument;
    private PGvector embedding;

    public DocumentChunk(String content, String sourceDocument, PGvector embedding) {
        this.content = content;
        this.sourceDocument = sourceDocument;
        this.embedding = embedding;
    }
}
