package com.example.demo.controller;

import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CommentCreateRequest req) {
        try {
            Comment saved = commentService.createComment(req);

            Map<String, Object> authorInfo = Map.of(
                    "userId", "U-" + saved.getAuthor().getId(),
                    "name", saved.getAuthor().getName()
            );

            Map<String, Object> data = Map.of(
                    "commentId", "C-" + String.format("%03d", saved.getCommentId()),
                    "ownerType", saved.getOwnerType(),
                    "ownerId", req.getOwnerId(),
                    "authorId", "U-" + saved.getAuthor().getId(),
                    "content", saved.getContent(),
                    "createdAt", saved.getCreatedAt().toString(),
                    "authorInfo", authorInfo
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
}
