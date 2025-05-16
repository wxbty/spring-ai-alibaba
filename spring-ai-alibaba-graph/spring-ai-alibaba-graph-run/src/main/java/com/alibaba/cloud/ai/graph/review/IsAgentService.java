package com.alibaba.cloud.ai.graph.review;

import dev.ai.alibaba.samples.executor.ToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.function.FunctionCallback;

public class IsAgentService {

    public final ToolService toolService;

    private final ChatClient chatClient;

    public IsAgentService(ChatClient.Builder chatClientBuilder, ToolService toolService) {
        var functions = toolService.agentFunctionsCallback().toArray(FunctionCallback[]::new);

        this.chatClient = chatClientBuilder.defaultSystem("You are a helpful AI Assistant answering questions.")
                .defaultFunctions(functions)
                .build();
        this.toolService = toolService;
    }

    public ChatResponse execute(String input) {
        return chatClient.prompt().user(input).call().chatResponse();
    }

    public ChatResponse executeByPrompt(String input, String prompt) {
        return chatClient.prompt(prompt).user(input).call().chatResponse();
    }

    public String generateLiteratureMetadata(String paperContent) {
        String prompt = String.format(
                "请从文献中提取以下元数据信息:" +
                        "- 文献类型[document_type]（学位论文/学术期刊/图书/会议）\n" +
                        "- 文献语言[language]（中文/英文）\n" +
                        "- 文献标题[title]\n" +
                        "- 作者[authors]（包括作者所属机构）\n" +
                        "- 发表/出版时间[publish_date]\n" +
                        "- 来源[source]（学校/期刊/出版社）\n" +
                        "- 研究问题[research_questions]\n" +
                        "- 全文概述[overview]（300-500字）\n" +
                        "- 核心观点[key_points]（列举3-5个）\n" +
                        "- 研究理论[research_theories]\n" +
                        "- 研究方法[research_methods]\n" +
                        "文献内容：\n%s" +
                        "### Constraint\n" +
                        "DO NOT include anything other than the JSON array in your response.",
                paperContent
        );

        ChatResponse response = execute(prompt);
        var output = response.getResult().getOutput();
        return output.getContent();

    }


    public String generate2LiteratureReview(String paper1Content, String paper2Content) {

        // 构造提示词
        String prompt = String.format(
                "基于提供的2篇文献，撰写一篇{字数要求}字的文献综述。" +
                        "要求：\n" +
                        "1. 结构必须包含：引言→主题分类→批判性分析→研究空白→结论\n" +
                        "2. 正文中所有观点必须标注来源[1][2]...\n" +
                        "3. 最后提供规范的参考文献列表\n" +
                        "【写作风格】专业严谨，避免简单罗列，注重综合评述和逻辑关系分析。\n" +
                        "【写作风格】专业严谨，避免简单罗列，注重综合评述和逻辑关系分析。\n" +
                        "论文1内容：\n%s\n\n" +
                        "论文2内容：\n%s", paper1Content, paper2Content
        );

        ChatResponse response = execute(prompt);
        var output = response.getResult().getOutput();
        return output.getContent();

    }

    public String generate2LiteratureReviewWithExtra(String paper1Content, String paper2Content, String extraPrompt) {

        // 构造提示词
        String prompt = String.format(
                "基于提供的2篇文献，撰写一篇{字数要求}字的文献综述。" +
                        "要求：\n" +
                        "1. 结构必须包含：引言→主题分类→批判性分析→研究空白→结论\n" +
                        "2. 正文中所有观点必须标注来源[1][2]...\n" +
                        "3. 最后提供规范的参考文献列表\n" +
                        "【写作风格】专业严谨，避免简单罗列，注重综合评述和逻辑关系分析。\n" +
                        "【其他限制】" + extraPrompt + "\n" +
                        "论文1内容：\n%s\n\n" +
                        "论文2内容：\n%s", paper1Content, paper2Content
        );

        ChatResponse response = execute(prompt);
        var output = response.getResult().getOutput();
        return output.getContent();

    }

    public String generateLiteratureReviewByExist(String review, String paperContent) {

        // 构造提示词
        String prompt = String.format(
                "基于提供的已有文献综述（多篇独立的文献综述）和新文献，把新文献内容集成到到已有文献综述中，撰写一篇2000字的新文献综述。" +
                        "要求：\n" +
                        "1. 结构必须包含：引言→主题分类→批判性分析→研究空白→结论\n" +
                        "2. 正文中所有观点必须标注来源[1][2]...\n" +
                        "3. 最后提供规范的参考文献列表\n" +
                        "【写作风格】专业严谨，避免简单罗列，注重综合评述和逻辑关系分析。\n" +
                        "已有的文献综述内容：\n%s\n\n" +
                        "论文内容：\n%s", review, paperContent
        );

        ChatResponse response = execute(prompt);
        var output = response.getResult().getOutput();
        return output.getContent();

    }

    public String generateLiteratureReviewByExistWithExtra(String review, String paperContent, String extraPrompt) {

        // 构造提示词
        String prompt = String.format(
                "基于提供的已有文献综述（多篇独立的文献综述）和新文献，把新文献内容集成到到已有文献综述中，撰写一篇2000字的新文献综述。" +
                        "要求：\n" +
                        "1. 结构必须包含：引言→主题分类→批判性分析→研究空白→结论\n" +
                        "2. 正文中所有观点必须标注来源[1][2]...\n" +
                        "3. 最后提供规范的参考文献列表\n" +
                        "【写作风格】专业严谨，避免简单罗列，注重综合评述和逻辑关系分析。\n" +
                        "【其他限制】" + extraPrompt + "\n" +
                        "已有的文献综述内容：\n%s\n\n" +
                        "新文献论文内容：\n%s", review, paperContent
        );

        ChatResponse response = execute(prompt);
        var output = response.getResult().getOutput();
        return output.getContent();

    }

}
