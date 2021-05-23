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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<PostListDto> getAll() {
        Optional<List<Post>> optPostList = postRepository.findAll();

        if(!optPostList.isPresent()) throw new PostNotExistException();

        List<Post> postList = optPostList.get();
        List<PostListDto> allPost = new ArrayList<>();

        for (Post p : postList) {
            PostListDto postListDto = PostListDto.builder()
                    .id(p.getId())
                    .category(p.getCategory())
                    .subject(p.getSubject())
                    .uploadDate(p.getUploadDate())
                    .status(p.getStatus())
                    .build();

            allPost.add(postListDto);
        }

        return allPost;
    }

    /**
     * id로 포스트 정보 가져오기
     */
    public ApiPostDetailDTO getByIdForApi(Long id) throws IOException {
        // 엔티티 가져오기
        Optional<Post> optPost = postRepository.findOne(id);

        // 포스트 존재여부 검사
        if(!optPost.isPresent()) throw new PostNotExistException();

        // 포스트 내용 가져오기
        Post findPost = optPost.get();
        String content = fileService.getContent(findPost.getFilePath(), findPost.getS3Key());

        // postDto 생성
        ApiPostDetailDTO apiPostDetailDTO = ApiPostDetailDTO.builder()
                .id(findPost.getId())
                .category(findPost.getCategory())
                .subject(findPost.getSubject())
                .content(content)
                .uploadDate(findPost.getUploadDate())
                .build();

        return apiPostDetailDTO;
    }

    /**
     * id로 포스트 정보 가져오기 for admin
     */
    public AdminPostDetailDTO getByIdForAdmin(Long id) throws IOException {
        // 포스트 엔티티
        Optional<Post> optPost = postRepository.findOne(id);
        if(!optPost.isPresent()) throw new PostNotExistException();

        Post post = optPost.get();

        // AdminPostDetailDTO 생성
        AdminPostDetailDTO adminPostDetailDTO = AdminPostDetailDTO.builder()
                .id(post.getId())
                .category(post.getCategory())
                .subject(post.getSubject())
                .content(fileService.getContent(post.getFilePath(), post.getS3Key()))
                .uploadDate(post.getUploadDate())
                .status(post.getStatus())
                .build();

        return adminPostDetailDTO;
    }

    /**
     * 공개된 포스트 가져오기
     */
    public List<PostListDto> getPublicPosts() {
        Optional<List<Post>> optPostList = postRepository.findPublicPosts();

        if (!optPostList.isPresent()) throw new PostNotExistException();

        List<Post> postList = optPostList.get();
        List<PostListDto> posts = new ArrayList<>();
        for (Post post : postList) {
            PostListDto dto = PostListDto.builder()
                    .id(post.getId())
                    .category(post.getCategory())
                    .subject(post.getSubject())
                    .uploadDate(post.getUploadDate())
                    .status(post.getStatus())
                    .build();
            posts.add(dto);
        }

        return posts;
    }

    /**
     * 카테고리별 포스트
     */
    public List<PostListDto> getByCategory(String category) {
        Optional<List<Post>> optPostList = postRepository.findByCategory(category);

        if(!optPostList.isPresent()) throw new PostNotExistException();

        List<Post> postList = optPostList.get();
        List<PostListDto> postsByCategory = new ArrayList<>();
        for (Post post : postList) {
            PostListDto dto = PostListDto.builder()
                    .id(post.getId())
                    .category(post.getCategory())
                    .subject(post.getSubject())
                    .status(post.getStatus())
                    .uploadDate(post.getUploadDate())
                    .build();
            postsByCategory.add(dto);
        }

        return postsByCategory;
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

        // 카테고리 유효성 검사
        Optional<Category> optCategory = categoryRepository.findByName(category);
        Category postCategory = validateCategoryIsExist(optCategory);

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
        Optional<Post> optPost = postRepository.findOne(id);
        if (!optPost.isPresent()) throw new PostNotFoundException();
        Post targetPost = optPost.get();

        // 새로운 카테고리 엔티티
        Optional<Category> optCategory = categoryRepository.findByName(updateDto.getCategory());
        if(!optCategory.isPresent()) throw new CategoryNotFoundException();
        Category newCategory = optCategory.get();

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
    public void deleteById(Long id) {
        // 포스트 엔티티
        Optional<Post> optPost = postRepository.findOne(id);
        if (!optPost.isPresent()) throw new PostNotFoundException();
        Post targetPost = optPost.get();

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
        Optional<Post> optPost = postRepository.findOne(id);
        if(!optPost.isPresent()) throw new PostNotFoundException();

        Post targetPost = optPost.get();

        // 포스트가 임시상태인지 검사
        if(targetPost.getStatus() != PostStatus.TEMP)
            throw new PostNotTempException();

        // 포스트 파일
        File postFile = new File(targetPost.getFilePath());

        // 옮겨질 pathname
        String newPathname = createPathname(PostStatus.PUBLIC, targetPost.getCategory().getName(), targetPost.getSubject());

        // 포스트 temp → public 폴더 이동
        postFile.renameTo(new File(newPathname));

        // s3 포스트 temp → public으로 이동
        awsS3Service.move(targetPost.getS3Key(), newPathname);

        // db filePath, s3Key, status update
        targetPost.updateFilePath(newPathname);
        targetPost.updateS3Key(newPathname);
        targetPost.updateStatus(PostStatus.PUBLIC);
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
     * 카테고리 존재여부 검사
     */
    private Category validateCategoryIsExist(Optional<Category> optCategory) {
        if(!optCategory.isPresent()) throw new CategoryNotFoundException();
        return optCategory.get();
    }

    /**
     * 포스트 중복 검사
     */
    private void validateDuplicatePost(String subject) {
        postRepository.findBySubject(subject).ifPresent(p -> {
            throw new PostDuplicateException();
        });
    }
}
