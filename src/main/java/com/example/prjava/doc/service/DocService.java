package com.example.prjava.doc.service;


import com.example.prjava.doc.model.Folder;
import com.example.prjava.project.model.Project;
import com.example.prjava.project.repository.ProjectRepository;
import com.example.prjava.user.model.User;
import com.example.prjava.user.repository.UserRepository;
import com.example.prjava.user.security.UserDetailsImpl;
import com.example.prjava.doc.dto.DocRequestDto;
import com.example.prjava.doc.dto.DocResponseDto;
import com.example.prjava.doc.dto.StatusDto;
import com.example.prjava.doc.model.Doc;
import com.example.prjava.doc.dto.ResponseDto;
import com.example.prjava.doc.repository.DocRepository;
import com.example.prjava.doc.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DocService {

    private final DocRepository docRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;

    @Autowired
    public DocService(DocRepository docRepository, ProjectRepository projectRepository, UserRepository userRepository, FolderRepository folderRepository){
        this.docRepository = docRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
    }

    @Transactional
    public ResponseDto<Object> createDoc(Long projectId, DocRequestDto docRequestDto, UserDetailsImpl userDetails, Long folderId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        User user = userDetails.getUser();
        User user1 = userRepository.findById(docRequestDto.getInCharge()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));
        Folder folder = folderRepository.findById(folderId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 폴더입니다."));
        Doc doc = new Doc(project, docRequestDto, user, user1, folder);
        docRepository.save(doc);
        return new ResponseDto<>(true, "작성성공");
    }
    @Transactional
    public ResponseDto<?> getDocs(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        List<DocResponseDto> docs = new ArrayList<>();
        for(Doc doc : docRepository.findAllByProjectOrderByCreatedDateDesc(project)){
            docs.add(new DocResponseDto(doc.getId(), doc.getTitle(), doc.getUser().getNickname(), doc.getFolder().getId()));
        }
        return new ResponseDto<>(true, "요청성공", docs);
    }

    @Transactional
    public ResponseDto<Object> getDoc(Long docId) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        DocResponseDto docResponseDto = new DocResponseDto(doc);
        return  new ResponseDto<>(true,"요청성공", docResponseDto);
    }


    @Transactional
    public ResponseDto<Object> updateDoc(Long docId, DocRequestDto docRequestDto, UserDetailsImpl userDetails) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        if(!Objects.equals(userDetails.getUser().getId(), doc.getId())){
            return new ResponseDto<>(false,"작성자만 수정할 수 있습니다.");
        }
        User user = userRepository.findById(docRequestDto.getInCharge()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));
        doc.update(docRequestDto, user);
        return  new ResponseDto<>(true,"수정 성공.");
    }

    @Transactional
    public ResponseDto<Object> deleteDoc(Long docId, UserDetailsImpl userDetails) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        if(!Objects.equals(userDetails.getUser().getId(), doc.getId())){
            return new ResponseDto<>(false,"작성자만 삭제할 수 있습니다.");
        }
        docRepository.deleteById(docId);
        return new ResponseDto<>(true,"삭제 성공.");
    }

    @Transactional
    public ResponseDto<?> onGoing(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        List<DocResponseDto> docs = new ArrayList<>();
        for(Doc doc : docRepository.findAllByProjectOrderByCreatedDateDesc(project)){
            docs.add(new DocResponseDto(doc,doc.getInCharge()));
        }
        return new ResponseDto<>(true, "요청성공", docs);
    }

    @Transactional
    public ResponseDto<Object> docStatus(Long docId, StatusDto statusDto, UserDetailsImpl userDetails) {
        Doc doc = docRepository.findById(docId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 문서입니다."));
        if(!Objects.equals(userDetails.getUser().getId(), doc.getUser().getId())){
            return new ResponseDto<>(false,"작성자만 수정 가능합니다.");
        }
        doc.update(statusDto);
        return new ResponseDto<>(true, "수정 완료");
    }

    public ResponseDto<?> updateFolder(Long docId, Long folderId) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        Folder folder = folderRepository.findById(folderId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 폴더입니다."));
        doc.update(folder);
        return  new ResponseDto<>(true,"이동 성공.");
    }
}
