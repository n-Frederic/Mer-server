package com.example.demo.controller;

import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public Map<String, Object> getComments(
            @RequestParam String ownerType,
            @RequestParam String ownerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {

        Page<Comment> commentPage = commentService.getComments(ownerType, ownerId, page, pageSize);

        List<Map<String, Object>> list = commentPage.getContent().stream().map(c -> {

            Map<String, Object> authorInfo = Map.of(
                    "userId", "U-" + c.getAuthor().getId(),
                    "name", c.getAuthor().getName()
            );

            return Map.of(
                    "commentId", "C-" + String.format("%03d", c.getCommentId()),
                    "ownerType", c.getOwnerType(),
                    "ownerId", ownerId,
                    "authorId", "U-" + c.getAuthor().getId(),
                    "content", c.getContent(),
                    "createdAt", c.getCreatedAt().toString(),
                    "authorInfo", authorInfo
            );

        }).collect(Collectors.toList());

        return Map.of(
                "list", list,
                "total", commentPage.getTotalElements(),
                "page", page,
                "pageSize", pageSize
        );
    }
}
