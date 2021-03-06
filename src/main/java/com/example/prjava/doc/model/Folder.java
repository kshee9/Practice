package com.example.prjava.doc.model;

import com.example.prjava.project.model.Project;
import com.example.prjava.doc.dto.FolderRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Folder {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    public Folder(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    public void update(FolderRequestDto folderRequestDto) {
        this.name = folderRequestDto.getFolderName();
    }
}