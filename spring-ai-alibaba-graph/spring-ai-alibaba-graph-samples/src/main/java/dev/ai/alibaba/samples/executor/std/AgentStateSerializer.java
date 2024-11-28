package dev.ai.alibaba.samples.executor.std;

import com.alibaba.cloud.ai.graph.serializer.std.ObjectStreamStateSerializer;
import dev.ai.alibaba.samples.executor.state.Action;
import dev.ai.alibaba.samples.executor.state.Finish;
import dev.ai.alibaba.samples.executor.state.GeneralState;
import dev.ai.alibaba.samples.executor.state.Outcome;
import dev.ai.alibaba.samples.executor.state.Step;
import dev.ai.alibaba.samples.executor.std.json.ActionSerializer;
import dev.ai.alibaba.samples.executor.std.json.FinishSerializer;
import dev.ai.alibaba.samples.executor.std.json.OutcomeSerializer;
import dev.ai.alibaba.samples.executor.std.json.StepSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentStateSerializer extends ObjectStreamStateSerializer<GeneralState> {

	public AgentStateSerializer() {
		super(GeneralState::new);

		mapper().register(Outcome.class, new OutcomeSerializer());
		mapper().register(Finish.class, new FinishSerializer());
		mapper().register(Action.class, new ActionSerializer());
		mapper().register(Step.class, new StepSerializer());

	}

}
