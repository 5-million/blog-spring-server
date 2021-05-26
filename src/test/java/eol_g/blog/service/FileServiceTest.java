package eol_g.blog.service;

import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.post.PostNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks private FileService fileService;

    private final String postFolder = "test/public/spring";
    private final String pathname = "test/public/spring/subject.md";

    @BeforeEach
    void beforeEach() {
        File folder = new File(postFolder);
        if(!folder.exists()) folder.mkdirs();
    }

    @AfterEach
    void afterEach() {
        File file = new File(pathname);
        if(file.exists()) file.delete();

        File folder = new File(postFolder);
        if(folder.exists()) folder.delete();
    }

    @Test
    void 포스트_생성() throws IOException {
        //given
        String content = "content";

        //when
        File post = fileService.createFile(pathname, content);

        //then
        String createdContent = getContent(post);

        assertTrue(post.exists(), "파일이 존재해야합니다.");
        assertEquals(pathname, post.getPath(), "경로가 정확해야합니다.");
        assertEquals(content, createdContent, "입력된 내용이 정확해야합니다.");
    }

    @Test
    void 로컬에서_내용_가져오기() throws IOException {
        //given
        String content = "test content";

        File post = new File(pathname);
        post.createNewFile();
        writeContentToFile(content, post);

        //when
        String postContent = fileService.getContent(pathname);

        //then
        assertEquals(content, postContent, "내용이 정확해야합니다.");
        assertEquals(content.length(), postContent.length(), "내용의 길이가 정확해야합니다.");
    }

    /**
     * 파일이 로컬에 없는 경우
     */
    @Test
    void 포스트_파일이_없는_경우() throws IOException {
        //when
        PostNotFoundException thrown =
                assertThrows(PostNotFoundException.class, () -> fileService.getContent(pathname));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode(), "POST_NOT_FOUND 예외를 던져야합니다.");
    }
    
//    @Test
//    void 카테고리_폴더_생성() throws IOException {
//        //given
//        String name = "category";
//        File tempFolder = new File("test/temp");
//        tempFolder.mkdirs();
//
//        //when
//        fileService.crateCategoryFolder(name);
//
//        //then
//        File folderInTemp = new File("test/temp/" + name);
//        File folderInPublic = new File("test/public/" + name);
//
//        assertTrue(folderInTemp.exists(), "temp 폴더에 카테고리 폴더가 생성되어야 합니다.");
//        assertTrue(folderInPublic.exists(), "public 폴더에 카테고리 폴더가 생성되어야 합니다.");
//
//        tempFolder.delete();
//    }

    private String getContent(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);

        StringBuilder content = new StringBuilder();
        String readLine;
        while ((readLine = br.readLine()) != null) { // 줄 단위로 읽기
            content.append(readLine).append("\n"); // 줄 단위로 읽기 때문에 줄 끝에 개행문자 추가
        }

        return content.toString().trim();
    }

    private void writeContentToFile(String content, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}