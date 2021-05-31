package eol_g.blog.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Spy private FileService fileService;

    private final String publicFolder = "test/public/spring";
    private final String tempFolder = "test/temp/spring";
    private final String pathname = "test/public/spring/subject.md";

    @BeforeEach
    void beforeEach() {
        File pubFolder = new File(publicFolder);
        if(!pubFolder.exists()) pubFolder.mkdirs();

        File tmpFolder = new File(tempFolder);
        if(!tmpFolder.exists()) tmpFolder.mkdirs();
    }

    @AfterEach
    void afterEach() {
        File file = new File(pathname);
        if(file.exists()) file.delete();

        File pubFolder = new File(publicFolder);
        if(pubFolder.exists()) pubFolder.delete();

        File tmpFolder = new File(tempFolder);
        if(tmpFolder.exists()) tmpFolder.delete();
    }

    @Test
    void 파일_생성() throws IOException {
        //given
        String content = "content";

        //when
        File result = fileService.createFile(pathname, content);

        //then
        assertTrue(result.exists());
        assertEquals(pathname, result.getPath());
        assertEquals(content, fileService.getContent(pathname));
    }

    @Test
    void 로컬에서_파일내용_가져오기() throws IOException {
        //given
        String content = "content";
        File testFile = fileService.createFile(pathname, content);

        //when
        String result = fileService.getContent(pathname);

        //then
        assertEquals(content, result);
        assertEquals(content.length(), result.length());
    }

    @Test
    void 존재하지_않는_파일에서_가져올_경우() throws IOException {
        //then
        FileNotFoundException thrown = assertThrows(FileNotFoundException.class, () -> fileService.getContent(pathname));
    }

    @Test
    void 폴더_생성() throws IOException {
        //given
        String testFolderPath1 = "test/public/folder";
        String testFolderPath2 = "test/temp/folder";

        //when
        fileService.createFolder(testFolderPath1, testFolderPath2);

        //then
        File result1 = new File(testFolderPath1);
        File result2 = new File(testFolderPath2);

        assertTrue(result1.exists());
        assertTrue(result2.exists());

        //after
        if(result1.exists()) result1.delete();
        if(result2.exists()) result2.delete();
    }

    @Test
    void 폴더_생성_이미_존재하는_폴더일_경우() {
        //given
        String testFolderPath1 = "test/public/folder";
        String testFolderPath2 = "test/temp/folder";

        File testFolder1 = new File(testFolderPath1);
        File testFolder2 = new File(testFolderPath2);
        testFolder1.mkdirs();

        //when
        FileAlreadyExistsException thrown = assertThrows(FileAlreadyExistsException.class, () -> fileService.createFolder(testFolderPath1, testFolderPath2));

        //then
        assertEquals(FileAlreadyExistsException.class, thrown.getClass());

        //after
        if(testFolder1.exists()) testFolder1.delete();
        if(testFolder2.exists()) testFolder2.delete();
    }

    @Test
    void move() throws IOException {
        //given
        String sourceFilePath = "test/temp/spring/test.md";
        String destinationFilePath = "test/public/spring/test.md";

        File source = new File(sourceFilePath);
        source.createNewFile();

        //when
        fileService.move(sourceFilePath, destinationFilePath);

        //then
        File destination = new File(destinationFilePath);
        assertFalse(source.exists());
        assertTrue(destination.exists());

        //after
        if(source.exists()) source.delete();
        if(destination.exists()) destination.delete();
    }

    @Test
    void writeContent() throws IOException {
        //given
        String content = "content";
        File file = new File(pathname);
        if(!file.exists()) file.createNewFile();

        //when
        fileService.writeContent(content, file);

        //then
        assertEquals(content, fileService.getContent(pathname));
    }
}