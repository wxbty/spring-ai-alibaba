package dev.ai.alibaba.samples.executor;

import dev.ai.alibaba.samples.executor.state.Action;

public record IntermediateStep(Action action, String observation) {
}
