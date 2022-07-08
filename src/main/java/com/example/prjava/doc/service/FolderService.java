package com.example.prjava.doc.service;

import com.example.prjava.doc.model.Folder;
import com.example.prjava.project.model.Project;
import com.example.prjava.project.repository.ProjectRepository;
import com.example.prjava.doc.dto.FolderRequestDto;
import com.example.prjava.doc.dto.ResponseDto;
import com.example.prjava.doc.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository, ProjectRepository projectRepository) {
        this.folderRepository = folderRepository;
        this.projectRepository = projectRepository;
    }

    // 프로젝트에 폴더 등록
    @Transactional
    public ResponseDto<?> addFolder(FolderRequestDto folderRequestDto, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        Folder folder = new Folder(folderRequestDto.getFolderName(),project );
        folderRepository.save(folder);
        return new ResponseDto<>(true, "등록 성공");
    }

    // 프로젝트에 등록된 모든 폴더 조회
    @Transactional
    public ResponseDto<?> getFolders(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        List<Folder> list = folderRepository.findAllByProject(project);

        return new ResponseDto<>(true, "요청 성공", list);
    }

    // 폴더명 수정
    @Transactional
    public ResponseDto<?> updateFolder(Long folderId, FolderRequestDto folderRequestDto) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        folder.update(folderRequestDto);
        return new ResponseDto<>(true,"수정 성공");
    }

    // 폴더 삭제
    @Transactional
    public ResponseDto<?> deleteFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        folderRepository.delete(folder);
        return new ResponseDto<>(true,"삭제 성공");
    }
}