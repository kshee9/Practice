package com.example.prjava.doc.dto;

import com.example.prjava.doc.model.Folder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FolderResponseDto {
    private Long id;
    private String folderName;

    public FolderResponseDto(Folder folder) {
        this.id = folder.getId();
        this.folderName = folder.getName();
    }
}
