package com.example.prjava.project.util;

import com.example.prjava.project.exception.ExceptionMessage;
import com.example.prjava.project.model.Project;
import com.example.prjava.project.repository.ProjectRepository;
import com.example.prjava.user.model.User;
import com.example.prjava.user.security.JwtTokenProvider;
import com.example.prjava.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@RequiredArgsConstructor
public class CommonUtil {

    public static User getUser(){
        try {
            UserDetailsImpl userDetails= (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails == null) {
                throw new IllegalArgumentException(ExceptionMessage.REQUIRED_LOGIN);
            }
            return userDetails.getUser();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(ExceptionMessage.REQUIRED_LOGIN);
        }
    }
    public static User getUserByToken(String token, JwtTokenProvider jwtTokenProvider){
        if(token == null) return null;
        UserDetailsImpl userDetails = (UserDetailsImpl) jwtTokenProvider.getAuthentication(token).getPrincipal();
        return userDetails.getUser();
    }

    // Util
    public static Project getProject(Long projectId, ProjectRepository projectRepository) {
        return projectRepository.findById(projectId).orElseThrow(()-> new IllegalArgumentException(ExceptionMessage.NOT_EXIST_PROJECT));
    }
}
