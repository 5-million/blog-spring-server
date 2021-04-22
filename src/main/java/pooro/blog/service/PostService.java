package pooro.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pooro.blog.domain.Category;
import pooro.blog.domain.Post;
import pooro.blog.domain.PostStatus;
import pooro.blog.dto.PostUploadDto;
import pooro.blog.exception.category.CategoryNotFoundException;
import pooro.blog.exception.post.PostDuplicateException;
import pooro.blog.repository.CategoryRepository;
import pooro.blog.repository.PostRepository;

import java.io.File;
import java.io.IOException;
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
        Optional<Category> optionalCategory = categoryRepository.findByName(category);
        Category postCategory = getCategory(optionalCategory);

        // 파일 생성
        File file = fileService.createPost(status.toString().toLowerCase(),
                category,
                subject,
                content);

        // S3에 업로드
        String path = "posts/" + status.toString().toLowerCase() + "/" + category + "/";
        String s3Key = awsS3Service.upload(path, file);

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

    private Category getCategory(Optional<Category> optionalCategory) {
        Category postCategory;

        if(optionalCategory.isPresent()) postCategory = optionalCategory.get();
        else throw new CategoryNotFoundException();

        return postCategory;
    }

    private void validateDuplicatePost(String subject) {
        // subject 중복 검사
        postRepository.findBySubject(subject).ifPresent(p -> {
            throw new PostDuplicateException();
        });
    }
}
