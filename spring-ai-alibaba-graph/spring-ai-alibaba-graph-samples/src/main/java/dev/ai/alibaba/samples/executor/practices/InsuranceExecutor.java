package dev.ai.alibaba.samples.executor.practices;

import com.alibaba.cloud.ai.graph.GraphStateException;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.serializer.StateSerializer;
import dev.ai.alibaba.samples.executor.AgentService;
import dev.ai.alibaba.samples.executor.state.Action;
import dev.ai.alibaba.samples.executor.state.Finish;
import dev.ai.alibaba.samples.executor.state.GeneralState;
import dev.ai.alibaba.samples.executor.state.Outcome;
import dev.ai.alibaba.samples.executor.state.Step;
import dev.ai.alibaba.samples.executor.std.AgentStateSerializer;
import dev.ai.alibaba.samples.executor.std.json.JSONStateSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

@Slf4j
@Service
public class InsuranceExecutor {

	public enum Serializers {

		JSON(new JSONStateSerializer());

		private final StateSerializer<GeneralState> serializer;

		Serializers(StateSerializer<GeneralState> serializer) {
			this.serializer = serializer;
		}

		public StateSerializer<GeneralState> object() {
			return serializer;
		}

	}

	public class GraphBuilder {

		private StateSerializer<GeneralState> stateSerializer;

		public GraphBuilder stateSerializer(StateSerializer<GeneralState> stateSerializer) {
			this.stateSerializer = stateSerializer;
			return this;
		}

		public StateGraph<GeneralState> build() throws GraphStateException {
			if (stateSerializer == null) {
				stateSerializer = new AgentStateSerializer();
			}

			return new StateGraph<>(GeneralState.SCHEMA, stateSerializer).addEdge(START, "agent") // 下一个节点
				.addNode("agent", node_async(InsuranceExecutor.this::callAgent)) // 调用llm
				.addNode("action", InsuranceExecutor.this::executeTools) // 独立节点
				.addConditionalEdges( // 条件边，在agent节点之后
						"agent", edge_async(InsuranceExecutor.this::shouldContinue), // 根据agent的结果，进行条件判断
						Map.of("continue", "action", "end", END) // 不同分支，使action不再独立
				)
				.addEdge("action", "agent") // action后返回agent，非条件边
			;

		}

	}

	public final GraphBuilder graphBuilder() {
		return new GraphBuilder();
	}



	private final AgentService agentService;

	public InsuranceExecutor(AgentService agentService) {
		this.agentService = agentService;
	}

	Map<String, Object> callAgent(GeneralState state) {
		log.info("callAgent");

		var input = state.input().orElseThrow(() -> new IllegalArgumentException("no input provided!"));

		var intermediateSteps = state.intermediateSteps();

		var response = agentService.execute(input, intermediateSteps);

		var output = response.getResult().getOutput();

		if (output.hasToolCalls()) {

			var action = new Action(output.getToolCalls().get(0), "");

			return Map.of(GeneralState.AGENT_OUTCOME, new Outcome(action, null));

		}
		else {
			var finish = new Finish(Map.of("returnValues", output.getContent()), output.getContent());

			return Map.of(GeneralState.AGENT_OUTCOME, new Outcome(null, finish));
		}
	}

	CompletableFuture<Map<String, Object>> executeTools(GeneralState state) {
		log.trace("executeTools");

		var agentOutcome = state.agentOutcome()
			.orElseThrow(() -> new IllegalArgumentException("no agentOutcome provided!"));

		return agentService.toolService.executeFunction(agentOutcome.action().toolCall())
			.thenApply(result -> Map.of("intermediate_steps", new Step(agentOutcome.action(), result.responseData())));
	}

	String shouldContinue(GeneralState state) {

		return state.agentOutcome().map(Outcome::finish).map(finish -> "end").orElse("continue");
	}

}
