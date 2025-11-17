package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CommentCreateRequest req) {
        try {
            Comment saved = commentService.createComment(req);

//            Map<String, Object> authorInfo = Map.of(
//                    "userId", "U-" + saved.getAuthor().getId(),
//                    "name", saved.getAuthor().getName()
//            );

            Map<String, Object> data = Map.of(
                    "commentId", "C-" + String.format("%03d", saved.getCommentId()),
                    "ownerId", req.getOwnerId(),
                    "logId", req.getLogId(),
                    "content", saved.getContent(),
                    "createdAt", saved.getCreatedAt().toString()

            );

            return ResponseEntity
                    .status(201)
                    .body(Map.of(
                            "code", 201,
                            "message", "评论创建成功",
                            "data", data
                    ));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "code", 400,
                            "message", "评论创建失败"
                    ));
        }
    }
    @GetMapping
    public Map<String, Object> getComments(
            @RequestParam String logId,
            @RequestParam String ownerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {

        Page<Comment> commentPage = commentService.getComments(logId, page, pageSize);

        List<Map<String, Object>> list = commentPage.getContent().stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("commentId", c.getCommentId());
                    m.put("logId", c.getLogId());
                    m.put("ownerId", c.getOwnerId());
                    m.put("content", c.getContent());
                    m.put("createdAt", c.getCreatedAt().toString());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("comments", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", commentPage.getTotalElements());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);

        return result;
    }

    @DeleteMapping("/{commentId}")
    public Map<String, Object> deleteComment(
            @PathVariable String commentId
    ) {
        try {
            Long user=UserContext.getCurrentUserId();
            String userId=user.toString();
            commentService.deleteComment(commentId, userId);

            return Map.of(
                    "code", 200,
                    "message", "评论删除成功",
                    "data", Map.of("commentId", commentId)
            );

        } catch (SecurityException e) {
            return Map.of(
                    "code", 403,
                    "message", "无权删除此评论"
            );
        } catch (RuntimeException e) {
            return Map.of(
                    "code", 404,
                    "message", "删除评论失败"
            );
        }
    }
}
