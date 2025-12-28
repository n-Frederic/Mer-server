package com.example.demo.service;

import com.example.demo.context.UserContext;
import com.example.demo.dto.CommentCreateRequest;
import com.example.demo.entity.*;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final EventLogRepository eventLogRepository;
    private final NotificationRepository notificationRepository;

    public CommentService(CommentRepository commentRepository,EventLogRepository eventLogRepository,
                          UserRepository userRepository,LogRepository logRepository,NotificationRepository notificationRepository) {
        this.commentRepository = commentRepository;
        this.eventLogRepository = eventLogRepository;
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.notificationRepository = notificationRepository;
    }

    public Comment createComment(CommentCreateRequest req) {

        Long OwnerId = UserContext.getCurrentUserId();
//        User Owner = userRepository.findById(OwnerId)
//                .orElseThrow(() -> new RuntimeException("用户不存在"));
//
//        // ownerId 需要把 T-001 → 1 / L-001 → 1


        Comment c = new Comment(
                req.getLogId(),
                req.getOwnerId(),
                req.getContent()
        );

        Log log = logRepository.findById(req.getLogId()).orElse(null);
        Long authorId=log.getAuthor().getId();
        Notification notification=new Notification(authorId,"log",log.getId(),"A new comment on your log: "+log.getSummary(),c.getContent(),false);
        notificationRepository.save(notification);

        eventLogRepository.save(new EventLog(OwnerId,"COMMENT", LocalDateTime.now(),"comment",c.getCommentId()));
        return commentRepository.save(c);
    }

    public Page<Comment> getComments( String ownerId, int page, int pageSize) {

        Long parsedOwnerId = parseId(ownerId);

        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

        return commentRepository.findByLogId(parsedOwnerId, pageable);
    }

    public void deleteComment(String commentIdStr, String userIdStr) {

        Long commentId = parseId(commentIdStr); // C-001 → 1
        Long userId = parseId(userIdStr);       // U-1001 → 1001

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND_USER"));

       boolean isAuthor = comment.getOwnerId().equals(userId);
        boolean isAdmin = user.getRoleId() == UserRole.ADMIN.getId();

        if (!isAdmin&&!isAuthor) {
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
