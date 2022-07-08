package com.example.prjava.project.service;

import com.example.prjava.project.exception.ExceptionMessage;
import com.example.prjava.project.model.Project;
import com.example.prjava.project.repository.CommentRepository;
import com.example.prjava.project.repository.ProjectRepository;
import com.example.prjava.project.repository.UserProjectRepository;
import com.example.prjava.project.dto.CommentDto;
import com.example.prjava.project.model.Comment;
import com.example.prjava.project.util.CommonUtil;
import com.example.prjava.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final UserProjectRepository userProjectRepository;
    @Transactional
    public Long createComment(Long projectId, CommentDto.Request commentRequestDto) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        Comment savedComment = commentRepository.save(Comment.builder()
                .user(user)
                .comment(commentRequestDto.getComment())
                .build());
        project.getComments().add(savedComment);
        return savedComment.getId();
    }
    @Transactional
    public void deleteComment(Long commentId) {
        User user = CommonUtil.getUser();
        Comment comment = commentRepository.findById(commentId).orElseThrow(()-> new IllegalArgumentException(ExceptionMessage.NOT_EXIST_COMMENT));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }
}
