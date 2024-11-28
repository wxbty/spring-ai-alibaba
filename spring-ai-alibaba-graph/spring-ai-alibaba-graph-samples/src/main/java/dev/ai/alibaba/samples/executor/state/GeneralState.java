package dev.ai.alibaba.samples.executor.state;

import com.alibaba.cloud.ai.graph.state.AgentState;
import com.alibaba.cloud.ai.graph.state.AppenderChannel;
import com.alibaba.cloud.ai.graph.state.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneralState extends AgentState {

    public static final String INPUT = "input";

    public static final String AGENT_OUTCOME = "outcome";

    public static final String INTERMEDIATE_STEPS = "intermediate_steps";

    public static Map<String, Channel<?>> SCHEMA = Map.of(INTERMEDIATE_STEPS, AppenderChannel.<Step>of(ArrayList::new));

    public GeneralState(Map<String, Object> initData) {
        super(initData);
    }

    public Optional<String> input() {
        return value(INPUT);
    }

    public Optional<Outcome> agentOutcome() {
        return value(AGENT_OUTCOME);
    }

    public List<Step> intermediateSteps() {
        return this.<List<Step>>value(INTERMEDIATE_STEPS).orElseGet(ArrayList::new);
    }

}
