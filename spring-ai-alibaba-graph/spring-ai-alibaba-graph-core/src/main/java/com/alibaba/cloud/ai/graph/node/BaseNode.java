package com.alibaba.cloud.ai.graph.node;

import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.state.AgentState;

public abstract class BaseNode<T extends AgentState> implements NodeAction<T> {

    protected String name;

}
