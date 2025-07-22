package com.example.ragchatbot.mapper;

import com.example.ragchatbot.model.DocumentChunk;
import com.pgvector.PGvector; // 导入 PGvector
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentChunkMapper {

    void save(DocumentChunk documentChunk);

    List<DocumentChunk> findNearestNeighbors(@Param("embedding") PGvector embedding, @Param("limit") int limit);

    List<DocumentChunk> findBySourceDocument(String sourceDocument);

    void deleteBySourceDocument(String sourceDocument);
}
