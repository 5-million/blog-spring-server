package pooro.blog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pooro.blog.domain.PostStatus;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AwsS3ServiceTest {

    @Autowired AwsS3Service awsS3Service;
    @Autowired FileService fileService;

    @Test
    void 파일_업로드() {
        //given
        PostStatus status = PostStatus.PUBLIC;
        String subject = "upload_test";
        String content = "테스트 업로드 \n test upload \n 😀😃😄";
        String category = "spring";

        String path = "posts/public/" + category + "/";
        String key = path + subject + ".md";

        File file = fileService.createPost(status.toString().toLowerCase(), category, subject, content);

        //when
        String fileKey = awsS3Service.upload(path, file);

        //then
        try {
            assertEquals(key, fileKey, "bucket에 업로드된 파일의 key가 정확해야합니다.");
            assertEquals(
                    fileService.getContent(key),
                    awsS3Service.getObjectContent(key),
                    "파일의 내용과 업로드된 파일의 내용이 같아야 합니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //after
        awsS3Service.delete(key);
        if(file.exists()) file.delete();
    }

}