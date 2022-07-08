package com.example.prjava.doc.repository;



import com.example.prjava.project.model.Project;
import com.example.prjava.doc.model.Doc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocRepository extends JpaRepository<Doc, Long> {
    List<Doc> findAllByProjectOrderByCreatedDateDesc(Project project);

    List<Doc> findAllByProjectAndNoticeOrderByCreatedDateDesc(Project project, boolean notice);

    List<Doc> findAllByProjectAndOnGoingOrderByCreatedDateDesc(Project project, boolean onGoing);
}
