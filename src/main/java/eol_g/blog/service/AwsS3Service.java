package eol_g.blog.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import eol_g.blog.exception.post.PostNotFoundException;

import java.io.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * file을 bucket에 업로드
     */
    public String upload(String objectKey, File sourceFile) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, objectKey, sourceFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return objectKey;
    }

    /**
     * 버켓에 이미지 업로드
     * @return 버켓의 이미지 객체 url
     */
    public String uploadImage(MultipartFile sourceFile) throws IOException {
        UUID uuid = UUID.randomUUID();
        String key = "images/" + uuid.toString() + "-" + sourceFile.getOriginalFilename();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(sourceFile.getContentType());
        objectMetadata.setContentLength(sourceFile.getSize());

        amazonS3Client.putObject(new PutObjectRequest(bucket, key, sourceFile.getInputStream(), objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3Client.getUrl(bucket, key).toString();
    }

    /**
     * bucket에 있는 file의 내용 가져오기
     */
    public String getObjectContent(String objectKey) throws IOException, PostNotFoundException {
        try {
            S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucket, objectKey));
            return getContent(object.getObjectContent());
        } catch (AmazonS3Exception e) {
            throw new PostNotFoundException();
        }
    }

    /**
     * bucket에서 파일 삭제
     */
    public void delete(String objectKey) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, objectKey));
    }

    /**
     * 객체 이동
     */
    public void move(String sourceObjectKey, String destinationObjectKey) {
        // 목적지에 객체 복사
        copy(sourceObjectKey, destinationObjectKey);

        // 기존 위치의 객체 삭제
        delete(sourceObjectKey);
    }

    /**
     * 객체 복사
     */
    private void copy(String sourceObjectKey, String destinationObjectKey) {
        amazonS3Client.copyObject(bucket, sourceObjectKey, bucket, destinationObjectKey);
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
