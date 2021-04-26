package pooro.blog.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pooro.blog.error.ErrorCode;
import pooro.blog.exception.post.PostNotFoundException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock private AwsS3Service awsS3Service;
    @InjectMocks private FileService fileService;

    private final String pathname = "posts/public/spring/subject.md";

    @AfterEach
    void afterEach() {
        File file = new File(pathname);
        if(file.exists()) file.delete();
    }

    @Test
    void 포스트_생성() throws IOException {
        //given
        String content = "content";

        //when
        File post = fileService.createPost(pathname, content);

        //then
        String createdContent = getContent(post);

        assertEquals(true, post.exists(), "파일이 존재해야합니다.");
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
        String postContent = fileService.getContent(pathname, pathname);

        //then
        assertEquals(content, postContent, "내용이 정확해야합니다.");
        assertEquals(content.length(), postContent.length(), "내용의 길이가 정확해야합니다.");
    }

    /**
     * 파일이 로컬에 없을 경우 s3에서 내용을 받아 파일을 생성하고 내용을 반환하는 경우
     */
    @Test
    void s3에서_내용_가져오기() throws IOException {
        //given
        String content = "test content";
        given(awsS3Service.getObjectContent(anyString())).willReturn(content);

        //when
        String postContent = fileService.getContent(pathname, pathname);

        //then
        File file = new File(pathname);
        String fileContent = getContent(file);

        assertEquals(true, file.exists(), "s3에서 받은 내용으로 파일을 생성해야합니다.");
        assertEquals(content, postContent, "포스트의 내용이 정확해야합니다.");
        assertEquals(content, fileContent, "새로 만들어진 파일의 내용이 정확해야합니다.");
    }

    /**
     * 파일이 로컬과 s3 모두에 존재하지 않을 경우
     */
    @Test
    void 포스트_파일이_없는_경우() throws IOException {
        //given
        given(awsS3Service.getObjectContent(anyString())).willThrow(new PostNotFoundException());

        //when
        PostNotFoundException thrown =
                assertThrows(PostNotFoundException.class, () -> fileService.getContent(pathname, pathname));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode(), "POST_NOT_FOUND 예외를 던져야합니다.");
    }

    private String getContent(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);

        String content = "";
        String readLine = null;
        while ((readLine = br.readLine()) != null) { // 줄 단위로 읽기
            content += readLine + "\n"; // 줄 단위로 읽기 때문에 줄 끝에 개행문자 추가
        }

        return content.trim();
    }
    private void writeContentToFile(String content, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}