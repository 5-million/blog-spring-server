package eol_g.blog.controller.api;

import eol_g.blog.dto.PostDto;
import eol_g.blog.dto.PostListDto;
import eol_g.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/blog/posts/**")
@RequiredArgsConstructor
public class ApiPostController {

    private final PostService postService;

    /**
     * 모든 포스트 가져오기
     */
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getAll() {
        return postService.getPublicPosts();
    }

    /**
     * id로 포스트 가져오기
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDto getById(@PathVariable("id") Long id) throws IOException {
        return postService.getById(id);
    }

    /**
     * 카테고리별 포스트 가져오기
     */
    @GetMapping("/category")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getByCategory(@RequestParam("name") String name) {
        return postService.getByCategory(name);
    }
}
