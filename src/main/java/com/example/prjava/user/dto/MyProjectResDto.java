package com.example.prjava.user.dto;

import com.example.prjava.project.model.Genre;
import com.example.prjava.project.model.Language;


import java.util.List;

public interface MyProjectResDto{
    Long getId();
    String getProjectName();
    String getProjectDescription();
    String getThumbnail();
    List<Language> getLanguage();
    List<Genre> getGenre();
    String getStep();
}
