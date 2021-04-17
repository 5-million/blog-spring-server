package pooro.blog.service;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private final FileService fileService = new FileService();

    @Test
    void 파일_생성() {
        //given
        String category = "spring";
        String subject = "테스트 제목 hihi";
        String content = "content\n내용\n😀";
        String extension = "md";

        //when
        File createFile = fileService.create(category, subject, content, extension);

        //then
        subject = subject.replace(" ", "_");
        String pathname = "posts/" + category + "/" + subject + ".md";
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