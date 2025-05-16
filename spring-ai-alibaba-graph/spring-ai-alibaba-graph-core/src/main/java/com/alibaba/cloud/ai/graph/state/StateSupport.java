package com.alibaba.cloud.ai.graph.state;

import java.util.Map;

public class StateSupport {

    private Map<String, Object> context;

    public StateSupport(Map<String, Object> context) {
        this.context = context;
    }

    public Map<String, Object> end(Object value) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        context.put(NodeState.EXCEPTION, value);
        context.put(NodeState.OUTPUT, value);
        return Map.copyOf(context);
    }

    public static StateSupport of(Map<String, Object> context) {
        return new StateSupport(context);
    }

    public static Map<String, Object> endOf(Object value) {
        return Map.of(NodeState.EXCEPTION, value, NodeState.OUTPUT, value);
    }
}
