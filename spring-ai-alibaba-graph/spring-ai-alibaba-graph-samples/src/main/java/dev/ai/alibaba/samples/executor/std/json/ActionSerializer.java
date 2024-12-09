package dev.ai.alibaba.samples.executor.std.json;

import com.alibaba.cloud.ai.graph.serializer.Serializer;
import dev.ai.alibaba.samples.executor.state.Action;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ActionSerializer implements Serializer<Action> {

	@Override
	public void write(Action action, ObjectOutput out) throws IOException {
		var ter = action.toolCall();
		out.writeUTF(ter.id());
		out.writeUTF(ter.type());
		out.writeUTF(ter.name());
		out.writeUTF(ter.arguments());
		out.writeUTF(action.log());

	}

	@Override
	public Action read(ObjectInput in) throws IOException, ClassNotFoundException {
		var toolCall = new AssistantMessage.ToolCall(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF());

		return new Action(toolCall, in.readUTF());

	}

}
