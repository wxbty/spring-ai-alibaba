package dev.ai.alibaba.samples.executor.std.json;

import com.alibaba.cloud.ai.graph.serializer.std.NullableObjectSerializer;
import dev.ai.alibaba.samples.executor.state.Action;
import dev.ai.alibaba.samples.executor.state.Finish;
import dev.ai.alibaba.samples.executor.state.Outcome;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class OutcomeSerializer implements NullableObjectSerializer<Outcome> {

    @Override
    public void write(Outcome object, ObjectOutput out) throws IOException {
        writeNullableObject(object.action(), out);
        writeNullableObject(object.finish(), out);
    }

    @Override
    public Outcome read(ObjectInput in) throws IOException, ClassNotFoundException {

        var action = readNullableObject(in).map(Action.class::cast).orElse(null);
        var finish = readNullableObject(in).map(Finish.class::cast).orElse(null);

        return new Outcome(action, finish);
    }


}
