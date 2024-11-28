package dev.ai.alibaba.samples.executor.std.json;

import com.alibaba.cloud.ai.graph.serializer.Serializer;
import dev.ai.alibaba.samples.executor.state.Action;
import dev.ai.alibaba.samples.executor.state.Step;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class StepSerializer implements Serializer<Step> {

	@Override
	public void write(Step object, ObjectOutput out) throws IOException {
		out.writeUTF(object.observation());
		out.writeObject(object.action());
	}

	@Override
	public Step read(ObjectInput in) throws IOException, ClassNotFoundException {
		String observation = in.readUTF();
		var action = (Action) in.readObject();
		return new Step(action, observation);
	}

}
