package com.example.ragchatbot.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {

    private String message;
    private String fileName;
    private int chunksCount;
    private boolean success;

    public static UploadResponse success(String fileName, int chunksCount) {
        UploadResponse response = new UploadResponse();
        response.setMessage("文档上传并处理成功");
        response.setFileName(fileName);
        response.setChunksCount(chunksCount);
        response.setSuccess(true);
        return response;
    }

    public static UploadResponse error(String message) {
        UploadResponse response = new UploadResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}
