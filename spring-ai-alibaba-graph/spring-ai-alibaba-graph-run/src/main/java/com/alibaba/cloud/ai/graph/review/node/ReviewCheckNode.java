package com.alibaba.cloud.ai.graph.review.node;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.state.NodeState;
import com.alibaba.cloud.ai.graph.state.StateSupport;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviewCheckNode implements NodeAction {


    private static final String prompt = "基于提供的%s篇文献，撰写一篇%s字的文献综述。" +
            "要求：\n" +
            "1. 结构必须包含：引言→主题分类→批判性分析→研究空白→结论\n" +
            "2. 正文中所有观点必须标注来源[1][2]...\n" +
            "3. 最后提供规范的参考文献列表\n" +
            "【写作风格】专业严谨，避免简单罗列，注重综合评述和逻辑关系分析。\n";

    private static final String CONFIRM_TEMPLATE =
            "综述任务生成，将使用以下信息,请确认是否继续:\n" +
                    "提示词如下：\n" +
                    prompt +
                    "文献列表如下：\n" +
                    "%s";
    private Db db = Db.use();

    @Override
    public Map<String, Object> apply(NodeState agentState) {

        var taskId = agentState.input()
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalArgumentException("no taskId provided!"));
        agentState.putContext("taskId", taskId);
        Entity task;
        List<Entity> results;
        try {
            task = db.queryOne("SELECT * FROM review_tasks where id = ?", taskId);
            if (task == null) {
                return StateSupport.endOf("任务不存在");
            }
            if (!"pending".equals(task.getStr("status"))) {
                return StateSupport.endOf("任务已完成");
            }

            // 查询表中的数据
            results = db.query("SELECT * FROM documents where task_id = ?", taskId);
            // 打印查询结果
            if (results.size() < 2) {
                return StateSupport.endOf("至少需要两篇文献才能生成综述");
            }

            List<String> titleList = new ArrayList<>();
            for (Entity entity : results) {
                String content = entity.getStr("title");
                titleList.add(content);
            }
            return Map.of(NodeState.OUTPUT, String.format(CONFIRM_TEMPLATE, results.size(), task.getStr("word_count_requirement"), String.join("\n", titleList)));
        } catch (SQLException e) {

            return StateSupport.endOf(e.getMessage());
        }

    }

}
