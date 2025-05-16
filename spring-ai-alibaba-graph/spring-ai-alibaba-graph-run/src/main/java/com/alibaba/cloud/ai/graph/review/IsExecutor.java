package com.alibaba.cloud.ai.graph.review;

import com.alibaba.cloud.ai.graph.GraphStateException;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.review.node.HumanNode;
import com.alibaba.cloud.ai.graph.review.node.PromptNode;
import com.alibaba.cloud.ai.graph.review.node.ReviewCheckNode;
import com.alibaba.cloud.ai.graph.review.node.ReviewGenerateNode;
import com.alibaba.cloud.ai.graph.serializer.StateSerializer;
import com.alibaba.cloud.ai.graph.serializer.agent.JSONStateSerializer;
import com.alibaba.cloud.ai.graph.state.NodeState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Slf4j
@Service
public class IsExecutor {

    public class GraphBuilder {

        private StateSerializer stateSerializer;

        public GraphBuilder stateSerializer(StateSerializer stateSerializer) {
            this.stateSerializer = stateSerializer;
            return this;
        }

        public StateGraph build() throws GraphStateException {
            if (stateSerializer == null) {
                stateSerializer = new JSONStateSerializer();
            }

            return new StateGraph(stateSerializer).addEdge(START, "task_guide")
                    .addNode("task_guide",
                            node_async(new PromptNode("欢迎使用综述生成任务，请输入任务id"))) // 调用llm
                    .addEdge("task_guide", "input_taskid")// 下一个节点
                    .addNode("input_taskid", node_async(new HumanNode()))
                    .addEdge("input_taskid", "confirm_task")// 下一个节点
                    .addNode("confirm_task",
                            node_async(new ReviewCheckNode())) // 调用llm
                    .addConditionalEdges( // 用户确认提示词及任务信息
                            "confirm_task", edge_async(IsExecutor.this::taskCheck),
                            Map.of("continue_after_check", "input_customer_confirm", "end", END))
//                    .addEdge("confirm_task", "input_customer_confirm")// 下一个节点
                    .addNode("input_customer_confirm", node_async(new HumanNode()))
                    .addConditionalEdges( // 用户确认提示词及任务信息
                            "input_customer_confirm", edge_async(IsExecutor.this::promptIntention),
                            Map.of("confirm", "review_generate", "prompt_need_add", "add_prompt"))

                    .addNode("review_generate", node_async(new ReviewGenerateNode()))
                    .addNode("add_prompt", node_async(new ReviewGenerateNode()))// 调用llm
                    .addEdge("review_generate", END)
                    .addEdge("add_prompt", END)
                    ;

        }

    }

    public final GraphBuilder graphBuilder() {
        return new GraphBuilder();
    }

    private final IsAgentService agentService;

    public IsExecutor(IsAgentService agentService) {
        this.agentService = agentService;
    }


    String promptIntention(NodeState state) {

        var input = state.input()
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalArgumentException("no input provided!"));

        log.info("input:{}", input);
//        var response = agentService.executeByPrompt(input, "分析输入语气，判断用户是否肯定的语气，如确认直接返回是，未确认则判断用户是否需要修改提示词内容，返回对应的内容");

//        var output = response.getResult().getOutput();
//        log.info("agent:{}", output.getContent());
        // 判断用户输入内容是否包括全部所需要信息（年龄、性别、学历……）
        if (input.equals("确认")) {
            return "confirm";
        } else {
            state.putContext("extra_prompt", input);
            return "prompt_need_add";
        }
    }

    String taskCheck(NodeState state) {

        if (state.containsKey(NodeState.EXCEPTION)) {
            return "end";
        } else {
            return "continue_after_check";
        }
    }

}
