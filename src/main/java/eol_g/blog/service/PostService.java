package eol_g.blog.service;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.*;
import eol_g.blog.exception.post.PostNotTempException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eol_g.blog.exception.category.CategoryNotFoundException;
import eol_g.blog.exception.post.PostNotExistException;
import eol_g.blog.exception.post.PostNotFoundException;
import eol_g.blog.exception.post.PostDuplicateException;
import eol_g.blog.repository.CategoryRepository;
import eol_g.blog.repository.PostRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final FileService fileService;
    private final AwsS3Service awsS3Service;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 모든 포스트 정보 가져오기
     */
    public List<PostListDto> getAllPostForAdmin() {
        List<Post> postList = getPostEntityList();

        return PostListDto.toDTOList(postList);
    }

    /**
     * 공개된 포스트 가져오기 for api
     */
    public List<PostListDto> getAllPostForApi() {
        // 공개된 포스트 엔티티 리스트 가져오기
        List<Post> postList = getPostEntityList().stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLIC)
                .collect(Collectors.toList());

        // 공개된 포스트가 존재 여부 검증
        if(postList.size() == 0) throw new PostNotExistException();

        return PostListDto.toDTOList(postList);
    }

    /**
     * id로 포스트 정보 가져오기 for admin
     */
    public AdminPostDetailDTO getByIdForAdmin(Long id) throws IOException {
        // 포스트 엔티티
        Post post = getPostEntityById(id);

        // 포스트 내용 가져오기
        String content = getPostContent(post);

        // AdminPostDetailDTO 생성
        return AdminPostDetailDTO.toDTO(post, content);
    }

    /**
     * id로 포스트 정보 가져오기 for api
     */
    public ApiPostDetailDTO getByIdForApi(Long id) throws IOException {
        // 엔티티 가져오기
        Post post = getPostEntityById(id);

        // 임시저장 상태의 포스트인 경우 포스트를 감춤
        if(post.getStatus() == PostStatus.TEMP)
            throw new PostNotExistException();

        // 포스트 내용 가져오기
        String content = getPostContent(post);

        // ApiPostDetailDTO 생성
        return ApiPostDetailDTO.toDTO(post, content);
    }

    /**
     * 카테고리별 공개된 포스트
     */
    public List<PostListDto> getByCategory(String category) {
        // 공개된 포스트 엔티티 리스트
        List<Post> postList = getPostEntityListByCategory(category);

        return PostListDto.toDTOList(postList);
    }

    /**
     * 포스트 업로드
     */
    @Transactional
    public Long upload(PostUploadDto postDto) throws IOException {
        PostStatus status = postDto.getStatus();
        String category = postDto.getCategory();
        String subject = postDto.getSubject();
        String content = postDto.getContent();

        // 포스트 중복 검사
        validateDuplicatePost(subject);

        // 카테고리 엔티티
        Category postCategory = getCategoryEntityByName(category);

        // 파일 생성
        String pathname = createPathname(status, category, subject);
        File file = fileService.createPost(pathname, content);

        // S3에 업로드
        String s3Key = awsS3Service.upload(pathname, file);

        // post db에 저장
        Post post = Post.builder()
                .category(postCategory)
                .subject(subject)
                .filePath(file.getPath())
                .s3Key(s3Key)
                .status(status)
                .build();

        Long postId = postRepository.save(post);

        // post id 리턴
        return postId;
    }

    /**
     * 포스트 수정
     */
    @Transactional
    public void update(Long id, PostUpdateDto updateDto) throws IOException {
        // 기존 포스트 엔티티
        Post targetPost = getPostEntityById(id);

        // 새로운 카테고리 엔티티
        Category newCategory = getCategoryEntityByName(updateDto.getCategory());

        // 기존 포스트 파일
        File postFile = new File(targetPost.getFilePath());

        // 파일의 내용을 수정
        fileService.writeContentToFile(updateDto.getContent(), postFile);

        // s3 객체 내용 수정
        awsS3Service.upload(targetPost.getS3Key(), postFile);

        // 포스트 파일의 제목과 카테고리를 수정
        String newPathname = createPathname(targetPost.getStatus(), updateDto.getCategory(), updateDto.getSubject());
        postFile.renameTo(new File(newPathname));

        // s3 객체의 제목과 카테고리를 수정
        awsS3Service.move(targetPost.getS3Key(), newPathname);

        // 엔티티 업데이트
        targetPost.update(newCategory, updateDto.getSubject(), newPathname, newPathname);
    }

    /**
     * 포스트 삭제
     */
    @Transactional
    public void delete(Long id) {
        // 포스트 엔티티
        Post targetPost = getPostEntityById(id);

        // File 포스트 삭제
        File postFile = new File(targetPost.getFilePath());
        postFile.delete();

        // s3에서 삭제
        awsS3Service.delete(targetPost.getS3Key());

        // DB 포스트 삭제
        postRepository.delete(targetPost);
    }

    /**
     * 임시저장 상태의 포스트를 공개 상태로 전환
     */
    @Transactional
    public void convertToPublic(Long id) {
        // 포스트 엔티티
        Post post = getPostEntityById(id);

        // 포스트가 임시상태인지 검사
        if(post.getStatus() != PostStatus.TEMP)
            throw new PostNotTempException();

        // 포스트 파일
        File postFile = new File(post.getFilePath());

        // 옮겨질 pathname
        String newPathname = createPathname(PostStatus.PUBLIC, post.getCategory().getName(), post.getSubject());

        // 포스트 temp → public 폴더 이동
        postFile.renameTo(new File(newPathname));

        // s3 포스트 temp → public으로 이동
        awsS3Service.move(post.getS3Key(), newPathname);

        // db filePath, s3Key, status update
        post.updateFilePath(newPathname);
        post.updateS3Key(newPathname);
        post.updateStatus(PostStatus.PUBLIC);
    }

    /**
     * 레포지토리에서 post entity를 가져옴
     * id에 해당하는 포스트가 없을 경우 PostNotFoundException 예외 발생
     */
    private Post getPostEntityById(Long id) {
        // 포스트 엔티티
        Optional<Post> optional = postRepository.findById(id);
        if (!optional.isPresent()) throw new PostNotFoundException();

        return optional.get();
    }

    /**
     * 모든 post entity list를 가져오는 함수
     * 포스트가 존재하지 않는 경우 PostNotExistException 예외 발생
     */
    private List<Post> getPostEntityList() {
        Optional<List<Post>> optional = postRepository.findAll();
        if (!optional.isPresent()) throw new PostNotExistException();

        return optional.get();
    }

    /**
     * 카테고리별 post entity list를 가져오는 함수
     * 검색 결과가 없을 경우 PostNotExistExeption 예외 발생
     */
    private List<Post> getPostEntityListByCategory(String category) {
        Optional<List<Post>> optional = postRepository.findByCategory(category);
        if(!optional.isPresent()) throw new PostNotExistException();

        return optional.get();
    }

    /**
     * FileService를 이용해 포스트의 내용을 가져오는 함수
     */
    private String getPostContent(Post post) throws IOException {
        return fileService.getContent(post.getFilePath(), post.getS3Key());
    }

    /**
     * 포스트 중복 검사
     */
    private void validateDuplicatePost(String subject) {
        postRepository.findBySubject(subject).ifPresent(p -> {
            throw new PostDuplicateException();
        });
    }

    /**
     * 저장될 파일의 pathname 생성
     */
    private String createPathname(PostStatus status, String category, String subject) {
        return "posts/" + status.toString().toLowerCase() + "/"
                + category + "/"
                + subject.replace(" ", "_")
                + ".md";
    }

    /**
     * 카테고리 이름으로 엔티티 가져오기
     */
    private Category getCategoryEntityByName(String name) {
        Optional<Category> optional = categoryRepository.findByName(name);
        if(!optional.isPresent()) throw new CategoryNotFoundException();

        return optional.get();
    }
}
