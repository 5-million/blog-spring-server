package pooro.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pooro.blog.domain.Category;
import pooro.blog.domain.Post;
import pooro.blog.domain.PostStatus;
import pooro.blog.dto.PostDto;
import pooro.blog.dto.PostListDto;
import pooro.blog.dto.PostUploadDto;
import pooro.blog.exception.category.CategoryNotFoundException;
import pooro.blog.exception.post.PostNotExistException;
import pooro.blog.exception.post.PostNotFoundException;
import pooro.blog.exception.post.PostDuplicateException;
import pooro.blog.repository.CategoryRepository;
import pooro.blog.repository.PostRepository;

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
     * 포스트 업로드
     */
    @Transactional
    public Long upload(PostUploadDto postDto) throws PostDuplicateException,
            CategoryNotFoundException,
            IOException {
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
     * id로 포스트 정보 가져오기
     */
    public PostDto getById(Long id) throws IOException, PostNotFoundException {
        // 엔티티 가져오기
        Optional<Post> optPost = postRepository.findOne(id);

        // 포스트 존재여부 검사
        if(!optPost.isPresent()) throw new PostNotExistException();

        // 포스트 내용 가져오기
        Post findPost = optPost.get();
        String content = fileService.getContent(findPost.getFilePath(), findPost.getS3Key());

        // postDto 생성
        PostDto postDto = PostDto.builder()
                .id(findPost.getId())
                .category(findPost.getCategory())
                .subject(findPost.getSubject())
                .content(content)
                .uploadDate(findPost.getUploadDate())
                .build();

        return postDto;
    }

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
