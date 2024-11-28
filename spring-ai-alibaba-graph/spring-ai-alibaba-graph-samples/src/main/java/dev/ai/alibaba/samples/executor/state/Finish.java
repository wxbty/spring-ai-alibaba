package dev.ai.alibaba.samples.executor.state;

import java.util.Map;

public record Finish(Map<String, Object> returnValues, String log) {
}
