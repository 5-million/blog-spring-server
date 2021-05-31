package eol_g.blog.controller.api;

import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.service.post.ApiPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApiPostController {

    private final ApiPostService postService;

    /**
     * 모든 포스트 가져오기
     */
    @GetMapping("/api/blog/posts")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDTO> getAll() {
        return postService.getAll();
    }

    /**
     * id로 포스트 가져오기
     */
    @GetMapping("/api/blog/posts/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDetailDTO getById(@PathVariable("id") Long id) throws IOException {
        return postService.getById(id);
    }

    /**
     * 카테고리별 포스트 가져오기
     */
    @GetMapping("/api/blog/posts/category")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDTO> getByCategory(@RequestParam("name") String name) {
        return postService.getByCategory(name);
    }
}
