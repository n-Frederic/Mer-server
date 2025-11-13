package com.example.demo.service;

import com.example.demo.context.UserContext;
import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public Comment createComment(CommentCreateRequest req) {

        Long authorId = UserContext.getCurrentUserId();
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // ownerId 需要把 T-001 → 1 / L-001 → 1
        Long parsedOwnerId = parseId(req.getOwnerId());

        Comment c = new Comment(
                req.getOwnerType(),
                parsedOwnerId,
                author,
                req.getContent()
        );

        return commentRepository.save(c);
    }

    public Page<Comment> getComments(String ownerType, String ownerId, int page, int pageSize) {

        Long parsedOwnerId = parseId(ownerId);

        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

        return commentRepository.findByOwnerTypeAndOwnerId(ownerType, parsedOwnerId, pageable);
    }

    public void deleteComment(String commentIdStr, String userIdStr) {

        Long commentId = parseId(commentIdStr); // C-001 → 1
        Long userId = parseId(userIdStr);       // U-1001 → 1001

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_USER"));

        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isAdmin = user.getRole_id() == UserRole.ADMIN.getId();

        if (!isAuthor && !isAdmin) {
            throw new SecurityException("FORBIDDEN");
        }

        commentRepository.delete(comment);
    }

    private Long parseId(String str) {
        if (str.contains("-")) {
            return Long.parseLong(str.split("-")[1]);
        }
        return Long.parseLong(str);
    }


}
