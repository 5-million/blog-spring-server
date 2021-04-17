package pooro.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class FileService {

    /**
     * project 실행 시 posts 폴더가 없을 경우 생성
     */
    public FileService() {
        File postsFolder = new File("posts");

        if(!postsFolder.exists()) {
            try {
                postsFolder.mkdir();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 파일 생성
     */
    public File create(String category, String subject, String content, String extension) {
        String pathname = "posts/" + category + "/"
                + subject.replace(" ", "_")
                + "." + extension;

        File file = new File(pathname);
        try {
            boolean result = file.createNewFile();

            if(result) log.info(pathname + " 생성 성공");
            else log.info(pathname + " 생성 실패");

            writeContentToFile(content, file); // content를 file에 작성
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * get 포스트 내용
     */
    public String getContent(String pathname) throws IOException {
        File targetFile = new File(pathname);
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