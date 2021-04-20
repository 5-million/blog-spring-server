package pooro.blog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pooro.blog.domain.Category;
import pooro.blog.domain.Post;
import pooro.blog.domain.PostStatus;
import pooro.blog.exception.DuplicatePostException;
import pooro.blog.repository.CategoryRepository;
import pooro.blog.repository.PostRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired PostService postService;
    @Autowired PostRepository postRepository;
    @Autowired FileService fileService;
    @Autowired AwsS3Service awsS3Service;
    @Autowired CategoryRepository categoryRepository;

    @Test
    void 포스트_업로드() {
        //given
        PostStatus status = PostStatus.PUBLIC;
        String subject = "upload";
        String content = "테스트 업로드 \n test upload \n 😀😃😄";
        String category = "spring";
        String key = "posts/public/" + category + "/" + subject.replace(" ", "_") + ".md";

        Category postCategory = Category.createCategory(category);
        categoryRepository.save(postCategory);

        //when
        Long postId = postService.upload(status, category, subject, content);
        Post post = postRepository.findOne(postId).get();

        //then
        try {
            assertEquals(subject, post.getSubject(), "제목이 정확해야 합니다.");
            assertEquals(key, post.getS3Key(), "S3 Object key가 정확해야합니다.");
            assertEquals(content, fileService.getContent(key), "입력된 내용과 파일에 저장된 내용이 같아야합니다.");
            assertEquals(content,
                    awsS3Service.getObjectContent(key),
                    "입력된 내용과 bucket에 업로드된 파일의 내용이 같아야 합니다.");
            assertEquals(
                    fileService.getContent(key),
                    awsS3Service.getObjectContent(key),
                    "파일에 저장된 내용과 bucket에 업로드된 파일의 내용이 같아야 합니다.");
            assertEquals(LocalDate.now(), post.getUploadDate(), "업로드 날짜가 정확해야 합니다.");
            assertEquals(category, post.getCategory().getName(), "카테고리가 정확해야 합니다.");
            assertEquals(status, post.getStatus(), "상태가 정확해야 합니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //after
        File file = new File(key);
        awsS3Service.delete(key);
        if(file.exists()) file.delete();
    }

    @Test
    void 포스트_제목_중복_예외() {
        //given
        PostStatus status = PostStatus.PUBLIC;
        String subject = "upload";
        String content = "테스트 업로드 \n test upload \n 😀😃😄";
        String category = "spring";
        String category2 = "jpa";
        String key = "posts/public/" + category + "/" + subject.replace(" ", "_") + ".md";

        Category postCategory = Category.createCategory(category);
        categoryRepository.save(postCategory);
        postService.upload(status, category, subject, content);

        //when
        DuplicatePostException exception1 = assertThrows(DuplicatePostException.class,
                () -> postService.upload(status, category, subject, content));
        DuplicatePostException exception2 = assertThrows(DuplicatePostException.class,
                () -> postService.upload(status, category2, subject, content));

        //then
        assertEquals("이미 존재하는 포스트입니다.", exception1.getMessage());
        assertEquals("이미 존재하는 포스트입니다.", exception2.getMessage());

        //after
        File file = new File(key);
        awsS3Service.delete(key);
        if(file.exists()) file.delete();
    }
}