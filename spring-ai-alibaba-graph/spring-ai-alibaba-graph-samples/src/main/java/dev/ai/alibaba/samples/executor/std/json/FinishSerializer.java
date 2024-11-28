package dev.ai.alibaba.samples.executor.std.json;

import com.alibaba.cloud.ai.graph.serializer.Serializer;
import dev.ai.alibaba.samples.executor.state.Finish;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

public class FinishSerializer implements Serializer<Finish> {

    @Override
    public void write(Finish object, ObjectOutput out) throws IOException {
        out.writeObject(object.returnValues());
        out.writeUTF(object.log());
    }

    @Override
    public Finish read(ObjectInput in) throws IOException, ClassNotFoundException {
        Map<String, Object> returnValues = (Map<String, Object>) in.readObject();
        String log = in.readUTF();
        return new Finish(returnValues, log);
    }

}
