package com.example.prjava.user.dto;

import com.example.prjava.project.model.Genre;
import com.example.prjava.project.model.Language;
import com.example.prjava.project.model.Zzim;

import java.util.List;

public class ZzimResDto {
   private Long projectId;
    private String projectName;
    private String projectDescription;
    private String thumbnail;
    private List<Language> language;
    private List<Genre> genre;
    private String step;

    public ZzimResDto(Zzim zzim) {

     this.projectId = zzim.getProject().getId();
     this.projectName = zzim.getProject().getProjectName();
     this.projectDescription = zzim.getProject().getProjectDescription();
     this.thumbnail = zzim.getProject().getThumbnail();
     this.language = zzim.getProject().getLanguages();
     this.genre = zzim.getProject().getGenres();
     this.step = zzim.getProject().getStep();
    }
}
