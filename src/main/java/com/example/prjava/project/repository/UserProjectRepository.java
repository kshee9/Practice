package com.example.prjava.project.repository;

import com.example.prjava.project.model.Project;
import com.example.prjava.project.model.UserProject;
import com.example.prjava.user.dto.MyProjectResDto;
import com.example.prjava.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProjectRepository extends JpaRepository<UserProject, Long> {

    boolean existsByProjectAndUserNot(Project project, User user);

    boolean existsByProjectAndUser(Project project, User user);
    boolean existsByProjectAndUserAndIsTeam(Project project, User user, boolean isTeam);

    List<UserProject> findAllByProject(Project project);
    // Optional<UserProject> findByProjectAndUserId(Project project, Long userId);
    Optional<UserProject> findByProjectAndUser(Project project, User user);


    List<MyProjectResDto> findAllByUserAndIsTeam(User user, boolean isTeam);

    List<MyProjectResDto> findAllByUserAndIsTeam(Optional<User> user, boolean isTeam);
}
