package com.alibaba.cloud.ai.graph.node;

import com.alibaba.cloud.ai.graph.state.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class StartNode extends BaseNode<AgentState> {

    private String welcomeMessage;

    @Override
    public Map<String, Object> apply(AgentState agentState) throws Exception {
        return null;
    }
}
