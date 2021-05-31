package eol_g.blog.controller.admin;

import eol_g.blog.dto.*;
import eol_g.blog.service.post.AdminPostService;
import eol_g.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService postService;
    private final CategoryService categoryService;

    /**
     * id로 포스트 가져오기
     */
    @GetMapping("/admin/blog/posts/{id}")
    public String getById(@PathVariable("id") Long id, Model model) throws IOException {
        PostDetailDTO post = postService.getById(id);
        List<String> categories = categoryService.getAll();

        model.addAttribute("post", post);
        model.addAttribute("categories", categories);

        return "update-post";
    }

    /**
     * 모든 포스트 가져오기
     */
    @GetMapping("/admin/blog/posts")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<PostListDTO> getAll() {
        return postService.getAll();
    }

    /**
     * 포스트 업로드
     */
    @PostMapping("/admin/blog/posts")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@RequestBody PostUploadDTO postDto) throws IOException {
        postService.upload(postDto);
    }

    /**
     * 포스트 업데이트
     */
    @PatchMapping("/admin/blog/posts/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable("id") Long id, @RequestBody PostUpdateDTO updateDto) throws IOException {
        postService.update(id, updateDto);
    }

    /**
     * 임시상태 포스트를 공개상태로 전환
     */
    @PatchMapping("/admin/blog/posts/release/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void convertToPublic(@PathVariable("id") Long id) {
        postService.release(id);
    }

    /**
     * 포스트 삭제
     */
    @DeleteMapping("/admin/blog/posts/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        postService.delete(id);
    }
}
