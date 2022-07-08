package com.example.prjava.project.repository;

import com.example.prjava.project.model.Project;
import com.example.prjava.project.model.Zzim;
import com.example.prjava.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZzimRepository extends JpaRepository<Zzim, Long> {

    boolean existsByUserAndProject(User user, Project project);

    void deleteByUserAndProject(User user, Project project);

    boolean existsByProjectAndUser(Project project, User user);
    long countByProject(Project project);

    void deleteByProject(Project project);

    List<Zzim> findAllByUser(User user);

    List<Zzim> findAllByUser(Optional<User> user);
}
