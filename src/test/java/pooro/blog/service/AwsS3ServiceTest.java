package pooro.blog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        String subject = "upload_test";
        String content = "테스트 업로드 \n test upload \n 😀😃😄";
        String category = "spring";

        String path = "posts/" + category + "/";
        String key = path + subject + ".md";
        String expectedUrl = "https://pooro-blog.s3.ap-northeast-2.amazonaws.com/" + key;

        File file = fileService.create(category, subject, content, "md");

        //when
        String fileUrl = awsS3Service.upload(path, file);

        //then
        try {
            assertEquals(expectedUrl, fileUrl, "bucket에 업로드된 파일의 url이 정확해야합니다.");
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