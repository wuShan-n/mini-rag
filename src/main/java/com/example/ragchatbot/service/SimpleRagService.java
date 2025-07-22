package com.example.ragchatbot.service;

import com.example.ragchatbot.mapper.DocumentChunkMapper;
import com.example.ragchatbot.model.ChatResponse;
import com.example.ragchatbot.model.DocumentChunk;
import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service("ragService")
@Slf4j
public class SimpleRagService implements RagService {

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Autowired
    @Qualifier("huggingFaceWebClient")
    private WebClient huggingFaceWebClient;

    @Autowired
    @Qualifier("chatWebClient")
    private WebClient chatWebClient;

    @Value("${api.chat.model}")
    private String chatModelName;

    private static final int TOP_K_RESULTS = 5;

    @Async
    @Transactional
    @Override
    public void processDocument(byte[] fileContent, String originalFilename) {
        try {
            log.info("【异步任务开始】处理文档: {}", originalFilename);
            // 【核心修改】将字节数组传给提取方法
            String content = extractTextFromFile(fileContent);
            List<String> chunks = splitTextIntoChunks(content);

            for (String chunk : chunks) {
                float[] embeddingArray = getEmbedding(chunk);
                PGvector embeddingVector = new PGvector(embeddingArray);

                DocumentChunk documentChunk = new DocumentChunk(chunk, originalFilename, embeddingVector);
                documentChunkMapper.save(documentChunk);
            }
            log.info("【异步任务成功】文档处理完成: {}, 保存了 {} 个文本块", originalFilename, chunks.size());

        } catch (Exception e) {
            log.error("【异步任务失败】处理文档 '{}' 时失败: {}", originalFilename, e.getMessage(), e);
        }
    }

    @Override
    public ChatResponse answerQuestion(String question) {
        try {
            log.info("处理问题: {}", question);
            float[] questionEmbeddingArray = getEmbedding(question);
            PGvector questionVector = new PGvector(questionEmbeddingArray);

            List<DocumentChunk> relevantChunks = documentChunkMapper
                    .findNearestNeighbors(questionVector, TOP_K_RESULTS);

            if (relevantChunks.isEmpty()) {
                return ChatResponse.error("没有找到相关的文档内容，请先上传相关文档。");
            }


            String context = relevantChunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.joining("\n\n"));

            String answer = generateAnswerFromLLM(context, question);

            List<String> sourceChunks = relevantChunks.stream()
                    .map(chunk -> {
                        String chunkContent = chunk.getContent();
                        return chunkContent.substring(0, Math.min(chunkContent.length(), 100)) + "...";
                    })
                    .collect(Collectors.toList());

            log.info("问题回答完成");
            return ChatResponse.success(answer, sourceChunks);

        } catch (Exception e) {
            log.error("回答问题失败: {}", e.getMessage(), e);
            return ChatResponse.error("回答问题失败: " + e.getMessage());
        }
    }


    private float[] getEmbedding(String text) {
        log.info("正在为文本获取本地向量: '{}...'", text.substring(0, Math.min(text.length(), 20)));
        Map<String, String> requestBody = Map.of("inputs", text);
        Map<String, Object> response = huggingFaceWebClient.post()
                .uri("/embed")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !"success".equals(response.get("status"))) {
            throw new RuntimeException("从本地模型服务获取向量失败: " + response.get("message"));
        }

        List<Double> embeddingDoubleList = (List<Double>) response.get("embedding");
        float[] embedding = new float[embeddingDoubleList.size()];
        for (int i = 0; i < embeddingDoubleList.size(); i++) {
            embedding[i] = embeddingDoubleList.get(i).floatValue();
        }
        return embedding;
    }



    /**
     * 【推荐优化】使用 Tika 提取文件内容，支持更多格式
     */
    private String extractTextFromFile(byte[] fileContent) throws Exception {
        Tika tika = new Tika();
        try (InputStream stream = new ByteArrayInputStream(fileContent)) {
            return tika.parseToString(stream);
        }
    }
    private List<String> splitTextIntoChunks(String text) {
        log.info("正在使用【按句子分割】策略进行文本分块...");
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.CHINA);
        iterator.setText(text);

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                chunks.add(sentence);
            }
        }

        // （可选）如果句子过长，还可以对长句子进行二次分割，这里为了简化，暂不实现
        // （可选）可以将多个短句子合并成一个chunk，避免chunk过小
        return chunks;
    }


    private String generateAnswerFromLLM(String context, String question) {
        log.info("正在调用大语言模型生成答案...");

        // 1. 构建一个高质量的Prompt
        String prompt = String.format(
                "请严格根据以下提供的上下文信息，用中文简洁地回答用户的问题。" +
                        "如果上下文信息与问题无关或不足以回答，请直接说“根据提供的文档，我无法回答这个问题”，不要自行推理或添加额外信息。" +
                        "\n\n--- 上下文 ---\n%s\n\n--- 用户问题 ---\n%s\n\n回答:",
                context,
                question
        );

        // 2. 构建请求体 (这里的结构需要根据您使用的API服务商进行调整, 这是一个通用的示例)
        Map<String, Object> requestBody = Map.of(
                "model", chatModelName, // 从 application.yml 注入
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.5 // 控制答案的创造性，对于RAG应设置较低的值
        );

        try {
            // 3. 调用外部聊天模型API
            // 【注意】这里的 .bodyToMono(Map.class) 和后续的JSON解析需要根据您API的实际返回格式进行调整
            Map<String, Object> response = chatWebClient.post()
                    .uri("/v1/chat/completions") // 请根据您的API服务商修改URI
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // 在生产环境中可以考虑使用响应式链

            // 4. 解析返回的JSON以获取答案
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
            log.error("从聊天模型API获取的响应格式不正确: {}", response);
            return "抱歉，无法从AI模型解析出有效的答案。";

        } catch (Exception e) {
            log.error("调用大语言模型API失败: {}", e.getMessage(), e);
            return "抱歉，我在与AI模型通信时遇到了一个内部错误。";
        }
    }
}
