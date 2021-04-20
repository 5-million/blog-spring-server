package pooro.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pooro.blog.domain.Category;
import pooro.blog.domain.Post;
import pooro.blog.domain.PostStatus;
import pooro.blog.exception.DuplicatePostException;
import pooro.blog.repository.CategoryRepository;
import pooro.blog.repository.PostRepository;

import java.io.File;

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
    public Long upload(PostStatus status, String category, String subject, String content) {
        // 포스트 중복 검사
        validateDuplicatePost(subject);

        // 파일 생성
        File file = fileService.createPost(status.toString().toLowerCase(),
                category,
                subject,
                content);

        // S3에 업로드
        String path = "posts/" + status.toString().toLowerCase() + "/" + category + "/";
        String s3Key = awsS3Service.upload(path, file);

        // post db에 저장
        Category postCategory = categoryRepository.findByName(category);
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

    private void validateDuplicatePost(String subject) {
        // subject 중복 검사
        postRepository.findBySubject(subject).ifPresent(p -> {
            throw new DuplicatePostException("이미 존재하는 포스트입니다.");
        });
    }
}
