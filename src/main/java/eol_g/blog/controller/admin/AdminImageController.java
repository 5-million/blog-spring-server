package eol_g.blog.controller.admin;

import eol_g.blog.exception.image.NotImageException;
import eol_g.blog.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/admin/blog/img/**")
@RequiredArgsConstructor
public class AdminImageController {

    private final AwsS3Service awsS3Service;

    /**
     * 이미지 업로드
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 이미지 파일 형식 검사
        String contentType = file.getContentType();
        if (!contentType.equals(IMAGE_GIF_VALUE)
                && !contentType.equals(IMAGE_PNG_VALUE)
                && !contentType.equals(IMAGE_JPEG_VALUE)) {
            throw new NotImageException();
        }

        // 이미지 파일 저장
        String url = awsS3Service.uploadImage(file);

        // response 객체 생성
        Map<String, String> response = new HashMap<>();
        response.put("filename", file.getOriginalFilename());
        response.put("url", url);

        return response;
    }
}
