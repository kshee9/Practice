package com.example.prjava.doc.repository;


import com.example.prjava.doc.model.Folder;
import com.example.prjava.project.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByProject(Project project);
}
