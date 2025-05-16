package com.alibaba.cloud.ai.graph.tool;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileProcessingService {

    public static String extractTextFromFile(MultipartFile file) throws IOException {
        // 这里简单实现，实际可能需要根据文件类型(PDF/DOCX等)使用不同解析器
        if (file.getContentType() != null && file.getContentType().contains("text")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            // 对于非文本文件，这里简单返回文件名
            // 实际项目中应使用PDF解析库如Apache PDFBox或DOCX解析库
            return "File content: " + file.getOriginalFilename();
        }
    }
}
