package pooro.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pooro.blog.service.AwsS3Service;

import java.io.IOException;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final AwsS3Service awsS3Service;

    /**
     * 이미지 업로드 엔드포인트
     * @return aws 버켓의 이미지 객체 url
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return awsS3Service.uploadImage(file);
    }
}
