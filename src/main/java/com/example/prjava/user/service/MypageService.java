package com.example.prjava.user.service;

import com.example.prjava.project.model.Language;
import com.example.prjava.project.model.Zzim;
import com.example.prjava.project.repository.UserProjectRepository;
import com.example.prjava.project.repository.ZzimRepository;
import com.example.prjava.project.util.S3Uploader;
import com.example.prjava.user.dto.*;
import com.example.prjava.user.model.User;
import com.example.prjava.user.repository.UserRepository;
import com.example.prjava.user.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MypageService {

    private final ZzimRepository zzimRepository;

    private final UserProjectRepository userProjectRepository;

    private final UserRepository userRepository;

    private  final S3Uploader s3Uploader;

    @Autowired
    public MypageService(ZzimRepository zzimRepository,
                         UserProjectRepository userProjectRepository,
                         UserRepository userRepository,
                         S3Uploader s3Uploader
    )
    {
        this.zzimRepository =zzimRepository;
        this.userProjectRepository =userProjectRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;

    }

    @Transactional
    public UserResponseDto<?> getuserInfo(MypageReqDto mypageReqDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String profileUrl = mypageReqDto.getProfileUrl();
        String role = mypageReqDto.getRole();
        String nickname = mypageReqDto.getNickname();
        List<String> language =mypageReqDto.getLanguage();
        String github = mypageReqDto.getGithub();
        String figma = mypageReqDto.getFigma();
        String intro = mypageReqDto.getIntro();

        User user = userDetails.getUser();

        // 내가  찜한 프로젝트 불러오기 나누기 1
        List<Zzim> Zzims = zzimRepository.findAllByUser(user);
        //  찜한 프로젝트에서 필요한 값만 담기 나누기 2
        List<ZzimResDto> Zzim = new ArrayList<>();

        for (Zzim zzim : Zzims) {
            ZzimResDto zzimResDto = new ZzimResDto(zzim);
            Zzim.add(zzimResDto);
        }

        // 내가 참여한 모든 프로 젝트들 불러오기
        List<MyProjectResDto> myproject = userProjectRepository.findAllByUserAndIsTeam(user, true);

        ResultDto resDto = new ResultDto(profileUrl,role,nickname,language,github,figma,intro,Zzim,myproject);

        return new UserResponseDto<>(true,"마이페이지 정보를 가져왔습니다.", resDto);
    }


    @Transactional
    public UserResponseDto deleteUser(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        user.setStatus(false);
        userRepository.save(user);
        return new UserResponseDto(true, "삭제성공");
    }
    @Transactional
    public UserResponseDto<?> updateUserInfo(UserDetailsImpl userDetails, MultipartFile file, MypageReqDto reqDto){
        String username = userDetails.getUsername();
        String profileUrl = "";
        s3Uploader.deleteFromS3(profileUrl);

        Optional<User> user = userRepository.findByUsername(username);


        if(!user.isPresent()) {
            throw new IllegalArgumentException ("등록되지 않은 사용자입니다.");
        }
        if(!file.isEmpty()) {
            //이미지 업로드
            profileUrl = s3Uploader.upload(file, reqDto.getProfileUrl());
        }

        String phoneNumber = reqDto.getPhoneNumber();
        String figma = reqDto.getFigma();
        String github = reqDto.getGithub();
        String email = reqDto.getEmail();
        String role = reqDto.getRole();
        List<Language> language = reqDto.getLanguage().stream().map((string)-> Language.builder().language(string).build()).collect(Collectors.toList());
        String nickname = reqDto.getNickname();
        String intro = reqDto.getIntro();

        int nicknameL = nickname.length();
        int introL = intro.length();

        if (nicknameL >10){
            throw new IllegalArgumentException("글자수가 초과되었습니다.");
        }
        if (nicknameL<2){
            throw  new IllegalArgumentException("글자수가 부족합니다.");
        }
        if (introL >20){
            throw new IllegalArgumentException("글자수가 초과되었습니다.");
        }


        MyUpdateDto myUpdateDto = new MyUpdateDto(profileUrl,role,nickname,reqDto.getLanguage(),github,figma,intro,phoneNumber,email);


        user.get().update(profileUrl,role,nickname,language,github,figma,intro,phoneNumber,email);

        // 트랜잭션때문에 안써도 됌
        //userRepository.save(user.get());

        return  new UserResponseDto<>(true,"수정 성공", myUpdateDto);


    }

    // 프로젝트 메인페이지에서 팀원 프로필 정보 불러오기용
    public UserResponseDto<?> OneUserInfo(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        String profileUrl = user.get().getProfileUrl();
        String role = user.get().getRole();
        String nickname = user.get().getNickname();
        List<String> language = user.get().getLanguage().stream().map(Language::getLanguage).collect(Collectors.toList());
        String github = user.get().getGithub();
        String figma = user.get().getFigma();
        String intro = user.get().getIntro();

        ProfileResDto profileResDto = new ProfileResDto(profileUrl,role,nickname,language,github,figma,intro);

        return new UserResponseDto<>(true,"유저 정보를 불러왔습니다.",profileResDto);
    }

}
