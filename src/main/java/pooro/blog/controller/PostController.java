package pooro.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pooro.blog.dto.PostUploadDto;
import pooro.blog.exception.category.CategoryNotFoundException;
import pooro.blog.exception.post.PostDuplicateException;
import pooro.blog.service.PostService;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 포스트 업로드 컨트롤러
     */
    @PostMapping(value = "/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@RequestBody PostUploadDto postDto)
            throws PostDuplicateException, CategoryNotFoundException, IOException {
        postService.upload(postDto);
    }
}
