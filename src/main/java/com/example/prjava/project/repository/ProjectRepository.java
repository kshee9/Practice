package com.example.prjava.project.repository;

import com.example.prjava.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByProjectNameContainsAndLanguages_LanguageAndGenres_GenreAndStep(String search, String language, String genre, String step);

}
