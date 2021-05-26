package eol_g.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eol_g.blog.exception.post.PostNotFoundException;

import java.io.*;

@Slf4j
@Service
public class FileService {

    /**
     * project 실행 시 posts 폴더가 없을 경우 생성
     */
    public FileService() {
        File postsPublicFolder = new File("posts/public");
        File postsTempFolder = new File("posts/temp");

        try {
            if (!postsPublicFolder.exists()) postsPublicFolder.mkdirs();
            if (!postsTempFolder.exists()) postsTempFolder.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    /**
     * 포스트 파일 생성
     */
    public File createPost(String pathname, String content) throws IOException {
        File file = new File(pathname);
        boolean result = file.createNewFile();

        if(result) log.info(pathname + " 생성 성공");
        else log.error(pathname + " 생성 실패");

        writeContentToFile(content, file); // content를 file에 작성

        return file;
    }

    /**
     * get 포스트 내용
     */
    public String getContent(String filePath) throws IOException {
        File file = new File(filePath);

        // 파일이 없을 경우 예외 발생
        if(!file.exists()) throw new PostNotFoundException();

        return readContentFromFile(file);
    }

    /**
     * public과 temp 폴더 안에 카테고리 폴더 생성
     */
    public void crateCategoryFolder(String name) throws IOException {
        // temp와 public 상태 모두 카테고리 폴더 할당
        File folderInTemp = new File("posts/temp/" + name);
        File folderInPublic = new File("posts/public/" + name);

        // 폴더 생성
        boolean resultInTemp = folderInTemp.mkdir();
        boolean resultInPublic = folderInPublic.mkdir();

        if (!(resultInTemp & resultInPublic)) throw new IOException("카테고리 폴더 생성 에러");
    }

    /**
     * 파일 이동 함수
     */
    public void move(String sourceFilePath, String destinationFilePath) {
        File source = new File(sourceFilePath);
        source.renameTo(new File(destinationFilePath));
    }

    /**
     * 내용을 파일에 쓰는 함수
     */
    public void writeContentToFile(String content, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }

    /**
     * 포스트 파일에서 내용을 읽는 함수
     */
    private static String readContentFromFile(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);

        String content = "";
        String readLine = null;
        while ((readLine = br.readLine()) != null) { // 줄 단위로 읽기
            content += readLine + "\n"; // 줄 단위로 읽기 때문에 줄 끝에 개행문자 추가
        }
        br.close();

        return content.trim();
    }
}
