-- RAG 智能问答机器人数据库初始化脚本

-- 1. 创建数据库（如果不存在）
-- CREATE DATABASE ragdb;

-- 2. 连接到数据库
-- \c ragdb;

-- 3. 创建 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 4. 验证扩展安装
SELECT * FROM pg_extension WHERE extname = 'vector';

-- 5. 创建文档块表（可选，Spring JPA 会自动创建）
CREATE TABLE IF NOT EXISTS document_chunk (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    source_document VARCHAR(255),
    embedding vector(768)
);


-- 创建新的 HNSW 索引
CREATE INDEX ON document_chunk USING hnsw (embedding vector_cosine_ops);



-- 7. 查看表结构
\d document_chunk;

