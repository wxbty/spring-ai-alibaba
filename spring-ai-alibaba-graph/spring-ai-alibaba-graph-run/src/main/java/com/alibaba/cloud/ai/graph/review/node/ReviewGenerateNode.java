package com.alibaba.cloud.ai.graph.review.node;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.review.IsAgentService;
import com.alibaba.cloud.ai.graph.state.NodeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReviewGenerateNode implements NodeAction {

    private Db db = Db.use();

    @Override
    public Map<String, Object> apply(NodeState agentState) {

        var taskId = agentState.getContext("taskId");
        var extraPrompt = agentState.getContext("extra_prompt").toString();

        List<Entity> results;
        try {
            // 查询表中的数据
            results = db.query("SELECT * FROM documents where task_id = ?", taskId);

            List<String> titleList = new ArrayList<>();
            for (Entity entity : results) {
                String content = entity.getStr("title");
                titleList.add(content);
            }

            // 查询表中的数据
            results = db.query("SELECT * FROM documents where task_id = ?", taskId);
            // 打印查询结果
            if (results.size() < 2) {
                agentState.putContext(NodeState.EXCEPTION, "至少需要两篇文献才能生成综述");
                return Map.of(NodeState.OUTPUT, "至少需要两篇文献才能生成综述");
            }

            log.info("results.size={}", results.size());

            IsAgentService agentService = SpringUtil.getBean(IsAgentService.class);
            String review;
            if (StringUtils.hasText(extraPrompt)) {
                review = agentService.generate2LiteratureReviewWithExtra(
                        results.get(0).getStr("content_text"),
                        results.get(1).getStr("content_text"),
                        extraPrompt
                );
            } else {
                review = agentService.generate2LiteratureReview(
                        results.get(0).getStr("content_text"),
                        results.get(1).getStr("content_text")
                );
            }
            if (results.size() > 2) {
                for (int i = 2; i < results.size(); i++) {
                    if (StringUtils.hasText(extraPrompt)) {
                        review = agentService.generateLiteratureReviewByExistWithExtra(
                                review,
                                results.get(i).getStr("content_text"),
                                extraPrompt
                        );
                    } else {
                        review = agentService.generateLiteratureReviewByExist(
                                review,
                                results.get(i).getStr("content_text")
                        );
                    }
                }
            }

            log.info("最终review={}", review);

            Entity reviewEntity = Entity.create()
                    .set("cn_review", review)
                    .set("status", "completed");
            Db.use().update(reviewEntity, Entity.create("review_tasks").set("id", taskId));

            return Map.of(NodeState.OUTPUT, review);

        } catch (SQLException e) {
            agentState.putContext(NodeState.EXCEPTION, e.getMessage());
            return Map.of(NodeState.OUTPUT, e.getMessage());
        }

    }

}
