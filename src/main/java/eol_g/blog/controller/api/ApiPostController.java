package eol_g.blog.controller.api;

import eol_g.blog.dto.ApiPostDetailDTO;
import eol_g.blog.dto.PostListDto;
import eol_g.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApiPostController {

    private final PostService postService;

    /**
     * 모든 포스트 가져오기
     */
    @GetMapping("/api/blog/posts")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getAll() {
        return postService.getAllPostForApi();
    }

    /**
     * id로 포스트 가져오기
     */
    @GetMapping("/api/blog/posts/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiPostDetailDTO getById(@PathVariable("id") Long id) throws IOException {
        return postService.getByIdForApi(id);
    }

    /**
     * 카테고리별 포스트 가져오기
     */
    @GetMapping("/api/blog/posts/category")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getByCategory(@RequestParam("name") String name) {
        return postService.getByCategory(name);
    }
}
