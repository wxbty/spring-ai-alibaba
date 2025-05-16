package com.alibaba.cloud.ai.graph.request;

import lombok.Data;

@Data
public class TaskRequest {

    //综述标题
    private String title;

    private Long taskId;

    //输出字数
    private Integer wordCountRequirement;

    //学科专业
    private String discipline;

    //学历水平
    private String educationLevel;

}
