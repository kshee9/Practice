package com.example.prjava.doc.controller;


import com.example.prjava.doc.service.DocService;
import com.example.prjava.user.security.UserDetailsImpl;
import com.example.prjava.doc.dto.ResponseDto;
import com.example.prjava.doc.dto.DocRequestDto;
import com.example.prjava.doc.dto.StatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocController {
    private final DocService docService;

    @Autowired
    public DocController(DocService docService){
        this.docService = docService;
    }

    //문서 생성
    @PostMapping("/api/doc/{projectId}/{folderId}")
    public ResponseDto<Object> createDoc(@PathVariable Long projectId,
                                         @PathVariable Long folderId,
                                         @RequestBody DocRequestDto docRequestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        return docService.createDoc(projectId, docRequestDto, userDetails, folderId);
    }

    //문서 리스트 조회
    @GetMapping("/api/docs/{projectId}")
    public ResponseDto<?> getDocs(@PathVariable Long projectId){ return docService.getDocs(projectId); }

    //문서 내용 조회
    @GetMapping("/api/doc/{docId}")
    public ResponseDto<Object> getDoc(@PathVariable Long docId){ return docService.getDoc(docId); }

    //문서 수정
    @PutMapping("/api/doc/{docId}")
    public ResponseDto<Object> updateDoc(@PathVariable Long docId,
                                         @RequestBody DocRequestDto docRequestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return docService.updateDoc(docId, docRequestDto, userDetails);
    }

    //문서 삭제
    @DeleteMapping("/api/doc/{docId}")
    public ResponseDto<Object> deleteDoc(@PathVariable Long docId,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        return docService.deleteDoc(docId, userDetails);
    }

    //게시판 문서조회
    @GetMapping("/api/onGoing/{projectId}")
    public ResponseDto<?> onGoing(@PathVariable Long projectId){
        return docService.onGoing(projectId);
    }

    //문서 진행상황 상태변경
    @PutMapping("/api/docStatus/{docId}")
    public ResponseDto<Object> docStatus(@PathVariable Long docId,
                                         @RequestBody StatusDto statusDto,
                                         UserDetailsImpl userDetails){
        return docService.docStatus(docId,statusDto, userDetails);
    }

    //문서 폴더이동
    @PutMapping("/api/doc/{docId}/{folderId}")
    public ResponseDto<?> updateFolder(@PathVariable Long docId,
                                       @PathVariable Long folderId){
        return docService.updateFolder(docId, folderId);
    }
}
