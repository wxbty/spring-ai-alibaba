/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.graph.controller;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.request.TaskRequest;
import com.alibaba.cloud.ai.graph.review.IsAgentService;
import com.alibaba.cloud.ai.graph.tool.FileProcessingService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/literature")
public class DocumentController {

    private Db db = Db.use();
    @Resource
    private IsAgentService agentService;


    @GetMapping("/api/index")
    public String index() {

        List<Entity> results = null;
        try {
            // 查询表中的数据
            results = db.query("SELECT * FROM documents");
            // 打印查询结果
            for (Entity entity : results) {
                System.out.println(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return JSONUtil.toJsonStr(results);
    }


    @PostMapping("/upload")
    public Long uploadLiterature(
            @RequestParam("paper") MultipartFile paper,
             TaskRequest taskRequest) {
//        ReviewTask reviewTask = BeanUtil.toBean(taskRequest, ReviewTask.class);
        Long taskId = taskRequest.getTaskId();
        if (taskId == null) {
            if (!StringUtils.hasText(taskRequest.getTitle())) {
                return 0L;
            }
            //任务入库
            Entity entity = Entity.create("review_tasks")
                    .set("title", taskRequest.getTitle());
            if (StringUtils.hasText(taskRequest.getDiscipline())) {
                entity.set("discipline", taskRequest.getDiscipline());
            }
            if (taskRequest.getWordCountRequirement() != null) {
                entity.set("word_count_requirement", taskRequest.getWordCountRequirement());
            }
            if (StringUtils.hasText(taskRequest.getEducationLevel())) {
                entity.set("education_level", taskRequest.getEducationLevel());
            }
            try {
                taskId = Db.use().insertForGeneratedKey(
                        entity
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            // 1. 从文件中提取文本内容
            String paperContent = FileProcessingService.extractTextFromFile(paper);

            // 2. 调用百炼大模型生成文献综述
            String review = agentService.generateLiteratureMetadata(paperContent);
            System.out.println("元数据: " + review);
            JSON json = JSONUtil.parse(review);
//            String review = agentService.generateLiteratureReview(2, paperContent, paper2Content);
            // 3. 保存元数据到数据库
            String authors = json.getByPath("authors").toString();
            System.out.println("作者: " + authors);
            Entity metadataEntity = Entity.create("documents")
                    .set("task_id", taskId)
                    .set("original_filename", paper.getOriginalFilename())
                    .set("file_type", paper.getContentType())
                    .set("file_size", paper.getSize())
                    .set("language", json.getByPath("language").toString())
                    .set("title", json.getByPath("title").toString())
                    .set("authors", authors)
//                    .set("publish_date", json.getByPath("publish_date"))
                    .set("source", json.getByPath("source").toString())
                    .set("research_questions", json.getByPath("research_questions").toString())
                    .set("overview", json.getByPath("overview").toString())
                    .set("key_points", json.getByPath("key_points").toString())
                    .set("research_theories", json.getByPath("research_theories").toString())
                    .set("research_methods", json.getByPath("research_methods").toString())
                    .set("content_text", paperContent)
                    .set("status", 0);
            Db.use().insert(
                    metadataEntity
            );
            // 返回结果
            return taskId;

        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @PostMapping("/generate-review")
    public String generateLiteratureReview(Long taskId) {
        //判空
        if (taskId == null) {
            return "任务ID不能为空";
        }

        //查询任务状态
        Entity task;
        //根据taskId 查询document,必须大于1篇才可以生成报告
        List<Entity> results;
        try {
            task = db.queryOne("SELECT * FROM review_tasks where id = ?", taskId);
            if (task == null) {
                return "任务不存在";
            }
            if (!"pending".equals(task.getStr("status"))) {
                return "任务已完成";
            }

            // 查询表中的数据
            results = db.query("SELECT * FROM documents where task_id = ?", taskId);
            // 打印查询结果
            if (results.size() < 2) {
                return "至少需要两篇文献才能生成综述";
            }
            String review;
            review = agentService.generate2LiteratureReview(
                    results.get(0).getStr("content_text"),
                    results.get(1).getStr("content_text")
            );
            if (results.size() > 2) {
                for (int i = 2; i < results.size(); i++) {
                    review = agentService.generateLiteratureReviewByExist(
                            review,
                            results.get(i).getStr("content_text")
                    );
                }
            }

            // 3. 保存结果到数据库
            Entity reviewEntity = Entity.create()
                    .set("cn_review", review)
                    .set("status", "completed");
            Db.use().update(reviewEntity, Entity.create("review_tasks").set("id", taskId));
            return review;
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }

    }


    @GetMapping("/query")
    public String queryByRag(String question) {

        String url = "http://117.72.42.149/v1/chat-messages";
        String apiKey = "app-jW7393C9zbCBNdjv3p9fkTnt";

        Map<String, Object> bodyJson = new HashMap<>();
        bodyJson.put("inputs", new HashMap<>());
        bodyJson.put("query", question);
        bodyJson.put("user", "abc-123");
        //   "response_mode" : "streaming"
        bodyJson.put("response_mode", "streaming");

        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(bodyJson))
                .execute();

        // 检查响应
        if (response.isOk()) {
            // 打印响应内容
            return "Response: " + response.body();
        } else {
            System.out.println(response.body());
            return "Request failed with status code: " + response.body();
        }
    }


}
