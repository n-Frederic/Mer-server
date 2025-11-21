package com.example.demo.controller;

import com.example.demo.context.UserContext;
import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {

        Page<Comment> commentPage = commentService.getComments(logId, page, pageSize);

        List<Long> ownerIds = commentPage.getContent().stream()
                .map(Comment::getOwnerId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

// 2. 构造返回结果
        List<Map<String, Object>> list = commentPage.getContent().stream()
                .map(c -> {
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("commentId", c.getCommentId());
                    commentMap.put("logId", c.getLogId());

                    // 构建 authorInfo
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("userId", "U-" + c.getOwnerId());

                    // 从预加载的用户Map中获取信息，并处理用户不存在的情况
                    User author = userMap.get(c.getOwnerId());
                    authorInfo.put("name", author != null ? author.getName() : "匿名用户");

                    commentMap.put("authorInfo", authorInfo);

                    commentMap.put("content", c.getContent());
                    commentMap.put("createdAt", c.getCreatedAt().toString());

                    return commentMap;
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
