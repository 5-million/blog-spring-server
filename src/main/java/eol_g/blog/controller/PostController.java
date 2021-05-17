package eol_g.blog.controller;

import eol_g.blog.dto.PostDto;
import eol_g.blog.dto.PostListDto;
import eol_g.blog.dto.PostUploadDto;
import eol_g.blog.exception.post.PostDuplicateException;
import eol_g.blog.exception.post.PostNotFoundException;
import eol_g.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import eol_g.blog.exception.category.CategoryNotFoundException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 모든 포스트 정보를 가져오는 컨트롤러
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getAll() throws PostNotFoundException {
        return postService.getAll();
    }

    /**
     * id로 포스트 정보를 가져오는 컨트롤러
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PostDto getOne(@PathVariable("id") Long id) throws IOException {
        return postService.getById(id);
    }

    /**
     * 포스트 업로드 컨트롤러
     */
    @PostMapping(value = "/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@RequestBody PostUploadDto postDto)
            throws PostDuplicateException, CategoryNotFoundException, IOException {
        postService.upload(postDto);
    }

    /**
     * 카테고리별 포스트 목록
     */
    @GetMapping("/category")
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDto> getByCategory(@RequestParam("name") String name) {
        return postService.getByCategory(name);
    }
}
