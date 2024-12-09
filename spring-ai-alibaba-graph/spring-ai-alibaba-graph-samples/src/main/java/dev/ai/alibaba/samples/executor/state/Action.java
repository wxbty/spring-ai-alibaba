package dev.ai.alibaba.samples.executor.state;

import org.springframework.ai.chat.messages.AssistantMessage;

public record Action(AssistantMessage.ToolCall toolCall, String log) {
}
