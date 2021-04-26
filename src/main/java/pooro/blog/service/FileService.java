package pooro.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pooro.blog.exception.post.PostNotFoundException;

import java.io.*;

@Slf4j
@Service
public class FileService {

    @Autowired private AwsS3Service awsS3Service;

    /**
     * project 실행 시 posts 폴더가 없을 경우 생성
     */
    public FileService() {
        File postsPublicFolder = new File("posts/PUBLIC");
        File postsTempFolder = new File("posts/TEMP");

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
    public File createPost(String pathname, String content) throws IOException{
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
    public String getContent(String pathname, String s3Key) throws IOException, PostNotFoundException {
        File targetFile = new File(pathname);

        // 파일이 없을 경우 s3에서 가져온다.
        if(!targetFile.exists()) {
            log.info("파일이 로컬 레포지토리에 존재하지 않습니다.");
            String objectContent = awsS3Service.getObjectContent(s3Key);

            boolean result = targetFile.createNewFile();
            if(result) {
                log.info(pathname + " 생성 성공");
                writeContentToFile(objectContent, targetFile);
            }

            else log.info(pathname + " 생성 실패");

            return objectContent;
        }

        FileReader fileReader = new FileReader(targetFile);
        BufferedReader br = new BufferedReader(fileReader);

        String content = "";
        String readLine = null;
        while ((readLine = br.readLine()) != null) { // 줄 단위로 읽기
            content += readLine + "\n"; // 줄 단위로 읽기 때문에 줄 끝에 개행문자 추가
        }

        return content.trim();
    }

    private static void writeContentToFile(String content, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }
}
