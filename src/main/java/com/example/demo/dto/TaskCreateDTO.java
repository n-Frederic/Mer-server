package com.example.demo.dto;

import com.example.demo.entity.Tags;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public class TaskCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;
    @NotBlank(message = "DDL不能为空")
    private Instant dueAt;
    @NotBlank(message = "优先级不能为空")
    private String priority;
    private List<String> tags;
    @NotBlank(message = "指派成员不能为空")
    private List<Long> assigneeIds;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    //    private String tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setAssigneeIds(List<Long> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }

//    public void setTags(String tags) {
//        this.tags = tags;
//    }

    public Instant getDueAt() {
        return dueAt;
    }

//    public String getTags() {
//        return tags;
//    }

    public String getPriority() {
        return priority;
    }

    public List<Long> getAssigneeIds() {
        return assigneeIds;
    }
}
