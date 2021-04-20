package pooro.blog.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * file을 bucket에 업로드
     */
    public String upload(String path, File file) {
        String key = path + file.getName();
        amazonS3Client.putObject(new PutObjectRequest(bucket, key, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return key;
    }

    /**
     * bucket에 있는 file의 내용 가져오기
     */
    public String getObjectContent(String key) throws IOException {
        S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucket, key));

        return getContent(object.getObjectContent());
    }

    /**
     * bucket에서 파일 삭제
     */
    public void delete(String key) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, key));
    }

    /**
     * InputStream을 한 줄씩 합쳐서 String으로 반환
     */
    private static String getContent(InputStream input) throws IOException {
        // Read the text input stream one line at a time
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String content = "";
        String line = null;

        while ((line = reader.readLine()) != null) {
            content += line + "\n";
        }

        return content.trim();
    }
}
