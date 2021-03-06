package com.example.prjava.project.service;

import com.example.prjava.doc.repository.DocRepository;
import com.example.prjava.project.dto.CommentDto;
import com.example.prjava.project.dto.DocDto;
import com.example.prjava.project.dto.ProjectDto;
import com.example.prjava.project.dto.UserDto;
import com.example.prjava.project.exception.ExceptionMessage;
import com.example.prjava.project.model.*;
import com.example.prjava.project.repository.ProjectQueryDslRepository;
import com.example.prjava.project.repository.ProjectRepository;
import com.example.prjava.project.repository.UserProjectRepository;
import com.example.prjava.project.repository.ZzimRepository;
import com.example.prjava.project.util.CommonUtil;
import com.example.prjava.project.util.S3Uploader;
import com.example.prjava.user.model.User;
import com.example.prjava.user.repository.UserRepository;
import com.example.prjava.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;
    private final ZzimRepository zzimRepository;
    private final DocRepository docRepository;
    private final ProjectQueryDslRepository projectQueryDslRepository;
    private final S3Uploader s3Uploader;
    private final String S3ThumbnailDir = "projectThumbnail";
    private final String S3InfoFileDir = "projectInfo";
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long createProject(ProjectDto.Request projectRequestDto, MultipartFile multipartFile, List<MultipartFile> infoFiles) {
        User user = CommonUtil.getUser();
        String thumbnailUrl = "";
        List<String> infoFileUrls = new ArrayList<>();
        if(multipartFile != null) {
            //????????? ?????????
            thumbnailUrl = s3Uploader.upload(multipartFile, S3ThumbnailDir);
        }
        if (infoFiles != null) {
            for (MultipartFile infoFile : infoFiles) {
                String infoFileUrl = s3Uploader.upload(infoFile, S3InfoFileDir);
                infoFileUrls.add(infoFileUrl);
            }
        }
        //save ????????? ???????????? ????????? ??????
        try {
            Project savedProject = projectRepository.save(Project.builder()
                    .thumbnail(thumbnailUrl)
                    .projectName(projectRequestDto.getProjectName())
                    .projectDescription(projectRequestDto.getProjectDescription())
                    .feCount(projectRequestDto.getFeCount())
                    .beCount(projectRequestDto.getBeCount())
                    .deCount(projectRequestDto.getDeCount())
                    .github(projectRequestDto.getGithub())
                    .figma(projectRequestDto.getFigma())
                    .deadLine(projectRequestDto.getDeadLine())
                    .step(projectRequestDto.getStep())
                    .languages(projectRequestDto.getLanguage().stream().map((string) -> Language.builder().language(string).build()).collect(Collectors.toList()))
                    .genres(projectRequestDto.getGenre().stream().map((string) -> Genre.builder().genre(string).build()).collect(Collectors.toList()))
                    .user(user)
                    .infoFiles(infoFileUrls)
                    .build());
            userProjectRepository.save(UserProject.builder()
                    .project(savedProject)
                    .isTeam(true)
                    .user(user)
                    .build());
            return savedProject.getId();
        } catch (Exception e) {
            log.info("delete Img");
            s3Uploader.deleteFromS3(s3Uploader.getFileName(thumbnailUrl));
            for (String infoFileUrl : infoFileUrls) {
                s3Uploader.deleteFromS3(s3Uploader.getFileName(infoFileUrl));
            }
            throw e;
        }

    }

    @Transactional(readOnly = true)
    public ProjectDto.Slice getProjects(String search, String language, String genre, String step, String token, int page, String sorted) {
        User user = CommonUtil.getUserByToken(token, jwtTokenProvider);
        // List<Project> list = projectQueryDslRepository.getProjectsBySearch(search, language, genre, step);
        // List<Project> list = projectRepository.findAllByProjectNameContainsAndLanguages_LanguageAndGenres_GenreAndStep("????????????", "spring", "???", "??????");

        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sorted);
        Pageable pageable = PageRequest.of(page, 18, sort);
        Slice<Project> slice = projectQueryDslRepository.getProjectsBySearch(search, language, genre, step, pageable);
        List<ProjectDto.Response> content = slice.getContent().stream().map(project -> {
            boolean isZzim;

            if(user==null) isZzim = false;
            else {
                isZzim = zzimRepository.existsByProjectAndUser(project,user);
            }
            int devCount = project.getBeCount() + project.getFeCount();
            int deCount = project.getDeCount();
            for (UserProject userProject : project.getUserProjects()) {
                String role = userProject.getUser().getRole();
                if ("back".equals(role) || "front".equals(role)) {
                    devCount--;
                } else if ("designer".equals(role)) {
                    deCount--;
                }
            }
            return ProjectDto.Response.builder()
                    .projectId(project.getId())
                    .thumbnail(project.getThumbnail())
                    .projectName(project.getProjectName())
                    .projectDescription(project.getProjectDescription())
                    .devCount(devCount)
                    .deCount(deCount)
                    .github(project.getGithub())
                    .figma(project.getFigma())
                    .deadLine(project.getDeadLine())
                    .dDay(
                            Duration.between(LocalDate.now().atStartOfDay(),project.getDeadLine().atStartOfDay()).toDays()
                    )
                    .step(project.getStep())
                    .language(project.getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList()))
                    .genre(project.getGenres().stream().map(Genre::getGenre).collect(Collectors.toList()))
                    .step(project.getStep())
                    .isZzim(isZzim)
                    .zzimCount(zzimRepository.countByProject(project))
                    .build();
        }).collect(Collectors.toList());
        return ProjectDto.Slice.builder()
                .isLast(!slice.hasNext())
                .list(content)
                .build();
    }

    @Transactional
    public void projectZzim(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (zzimRepository.existsByUserAndProject(user, project)) {
            //?????????
            zzimRepository.deleteByUserAndProject(user, project);
        }else {
            //?????????
            zzimRepository.save(Zzim.builder()
                    .user(user)
                    .project(project)
                    .build());
        }

    }

    @Transactional
    public ProjectDto.Response modifyProject(Long projectId, ProjectDto.Request projectRequestDto, MultipartFile multipartFile) {
        //TODO: language update??? ?????? ????????? ?????? ??????
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        String thumbnail = project.getThumbnail();
        if (multipartFile != null) {
            //???????????? ????????? ????????? multipartfile??? ?????? ????????? ??????
            //??????????????? ??????
            s3Uploader.deleteFromS3(s3Uploader.getFileName(project.getThumbnail()));
            //????????? ????????? ?????????
            thumbnail = s3Uploader.upload(multipartFile, S3ThumbnailDir);
        }

        return project.update(
                projectRequestDto.getProjectName(),
                projectRequestDto.getProjectDescription(),
                projectRequestDto.getFeCount(),
                projectRequestDto.getBeCount(),
                projectRequestDto.getDeCount(),
                projectRequestDto.getGithub(),
                projectRequestDto.getFigma(),
                projectRequestDto.getDeadLine(),
                projectRequestDto.getStep(),
                projectRequestDto.getLanguage().stream().map((string)-> Language.builder().language(string).build()).collect(Collectors.toList()),
                projectRequestDto.getGenre().stream().map((string)-> Genre.builder().genre(string).build()).collect(Collectors.toList()),
                thumbnail
        );
    }

    @Transactional
    public String modifyInfoFile(Long projectId, String fileUrl, MultipartFile infoFile) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        List<String> infoFiles = project.getInfoFiles();
        String infoFileUrl = null;
        //?????? ??????
        if (fileUrl != null) {
            //?????? ??????, ??????
            infoFiles.remove(fileUrl);
            s3Uploader.deleteFromS3(s3Uploader.getFileName(fileUrl));
        }
        if (infoFile != null) {
            //?????? ??????, ??????
            infoFileUrl = s3Uploader.upload(infoFile,S3InfoFileDir);
            infoFiles.add(infoFileUrl);
        }
        project.infoFilesUpdate(infoFiles);
        return infoFileUrl;
    }

    public boolean existUser(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return userProjectRepository.existsByProjectAndUserNot(project, user);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        if (project.getThumbnail() != null && !"".equals(project.getThumbnail())) {
            // ????????? ??????
            s3Uploader.deleteFromS3(s3Uploader.getFileName(project.getThumbnail()));
        }
        for (String infoFileUrl : project.getInfoFiles()) {
            s3Uploader.deleteFromS3(s3Uploader.getFileName(infoFileUrl));
        }
        zzimRepository.deleteByProject(project);
        projectRepository.delete(project);
    }


    public ProjectDto.Response getProject(Long projectId) {
        // User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        return ProjectDto.Response.builder()
                .thumbnail(project.getThumbnail())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .feCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("front", userProject.getUser().getRole())).count())
                .beCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("back", userProject.getUser().getRole())).count())
                .deCurrentCount((int) project.getUserProjects().stream().filter(userProject -> Objects.equals("designer", userProject.getUser().getRole())).count())
                .feCount(project.getFeCount())
                .beCount(project.getBeCount())
                .deCount(project.getDeCount())
                .github(project.getGithub())
                .figma(project.getFigma())
                .deadLine(project.getDeadLine())
                .step(project.getStep())
                .language(project.getLanguages().stream().map(Language::getLanguage).collect(Collectors.toList()))
                .genre(project.getGenres().stream().map(Genre::getGenre).collect(Collectors.toList()))
                .comment(
                        project.getComments().stream().map(comment -> CommentDto.Response.builder()
                                .commentId(comment.getId())
                                .nickname(comment.getUser().getNickname())
                                .comment(comment.getComment())
                                .build()).collect(Collectors.toList())
                )
                .infoFiles(project.getInfoFiles().stream().map(s ->
                        ProjectDto.File.builder()
                        .fileUrl(s)
                        .fileName(s3Uploader.getOriginalFileName(s,S3InfoFileDir))
                        .build())
                        .collect(Collectors.toList()))

                .build();
    }

    public void applyProject(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (userProjectRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_APPLY);
        }
        userProjectRepository.save(UserProject.builder()
                .user(user)
                .project(project)
                .isTeam(false)
                .build());
    }

    public ProjectDto.Response getProjectMain(Long projectId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!userProjectRepository.existsByProjectAndUserAndIsTeam(project, user, true)) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        return ProjectDto.Response.builder()
                .projectName(project.getProjectName())
                .feCount(project.getFeCount())
                .beCount(project.getBeCount())
                .deCount(project.getDeCount())
                .github(project.getGithub())
                .dDay(
                        Duration.between(LocalDate.now().atStartOfDay(),project.getDeadLine().atStartOfDay()).toDays()
                )
                .figma(project.getFigma())
                .user(
                        project.getUserProjects().stream().filter((UserProject::isTeam))
                                .map((userProjectList) -> {
                                    User userStream = userProjectList.getUser();
                                    return UserDto.builder()
                                            .userId(userStream.getId())
                                            .profileUrl(userStream.getProfileUrl())
                                            .role(userStream.getRole())
                                            .nickname(userStream.getNickname())
                                            .build();
                                }).collect(Collectors.toList())
                )
                .applyUser(
                        project.getUserProjects().stream().filter((userProject -> !userProject.isTeam()))
                                .map((userProjectList) -> {
                                    User userStream = userProjectList.getUser();
                                    return UserDto.builder()
                                            .userId(userStream.getId())
                                            .profileUrl(userStream.getProfileUrl())
                                            .role(userStream.getRole())
                                            .nickname(userStream.getNickname())
                                            .build();
                                }).collect(Collectors.toList())
                )
                .notice(
                        docRepository.findAllByProjectAndNoticeOrderByCreatedDateDesc(project, true).stream().map((doc)->{
                            return DocDto.builder()
                                    .docId(doc.getId())
                                    .title(doc.getTitle())
                                    .nickname(doc.getUser().getNickname())
                                    .createdDate(doc.getCreatedDate())
                                    .build();
                        }).collect(Collectors.toList())
                )
                .todo(
                        docRepository.findAllByProjectAndOnGoingOrderByCreatedDateDesc(project, true).stream().map((doc)->{
                            return DocDto.builder()
                                    .docId(doc.getId())
                                    .title(doc.getTitle())
                                    .inCharge(doc.getInCharge().getNickname())
                                    .createdDate(doc.getCreatedDate())
                                    .docStatus(doc.getDocStatus())
                                    .startDate(doc.getStartDate())
                                    .endDate(doc.getEndDate())
                                    .dDay(
                                            Duration.between(LocalDate.now().atStartOfDay(),doc.getEndDate().atStartOfDay()).toDays()
                                    )
                                    .build();
                        }).collect(Collectors.toList())
                )
                .build();
    }
    @Transactional
    public void addUser(Long projectId, Long userId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        User userSearch = userRepository.findById(userId).orElseThrow(()->
                new IllegalArgumentException("??")
        );
        UserProject userProject = userProjectRepository.findByProjectAndUser(project, userSearch).orElseThrow(()->
                new IllegalArgumentException(ExceptionMessage.NOT_APPLY)
        );
        if (userProject.isTeam()) {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_JOIN);
        }
        userProject.changeIsTeam(true);
    }

    public void kickUser(Long projectId, Long userId) {
        User user = CommonUtil.getUser();
        Project project = CommonUtil.getProject(projectId, projectRepository);
        if (!project.getUser().getId().equals(user.getId()) || userId.equals(user.getId())) {
            throw new IllegalArgumentException(ExceptionMessage.UNAUTHORIZED);
        }
        User userSearch = userRepository.findById(userId).orElseThrow(()->
                new IllegalArgumentException(ExceptionMessage.NOT_EXIST_USER)
        );
        UserProject userProject = userProjectRepository.findByProjectAndUser(project, userSearch).orElseThrow(()->
            new IllegalArgumentException(ExceptionMessage.NOT_APPLY)
        );
        userProjectRepository.delete(userProject);
    }

}
