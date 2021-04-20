package pooro.blog.service;

import org.junit.jupiter.api.Test;
import pooro.blog.domain.PostStatus;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private final FileService fileService = new FileService();

    @Test
    void 파일_생성() {
        //given
        PostStatus status = PostStatus.PUBLIC;
        String category = "spring";
        String subject = "테스트 제목 hihi";
        String content = "content\n내용\n😀";
        String extension = "md";

        //when
        File createFile = fileService.createPost(status.toString().toLowerCase(), category, subject, content);

        //then
        subject = subject.replace(" ", "_");
        String pathname = "posts/public/" + category + "/" + subject + ".md";
        File file = new File(pathname);

        try {
            assertEquals(true, file.exists());
            assertEquals(subject + ".md", createFile.getName());
            assertEquals(content, fileService.getContent(pathname));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //after
        if(createFile.exists()) createFile.delete();
    }

}