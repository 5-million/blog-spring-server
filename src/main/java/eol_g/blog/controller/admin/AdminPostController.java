package eol_g.blog.controller.admin;

import eol_g.blog.dto.PostListDto;
import eol_g.blog.dto.PostUploadDto;
import eol_g.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/blog/posts/**")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    /**
     * 모든 포스트 가져오기
     */
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getAll() {
        return postService.getAll();
    }

    /**
     * 포스트 업로드
     */
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@RequestBody PostUploadDto postDto) throws IOException {
        postService.upload(postDto);
    }
}
