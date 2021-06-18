package eol_g.blog.controller.admin;

import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostUploadDTO;
import eol_g.blog.service.CategoryService;
import eol_g.blog.service.post.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminPostService postService;
    private final CategoryService categoryService;

    @GetMapping("/admin/blog")
    public String homePage(Model model) {
        model.addAttribute("posts", postService.getAll());
        return "index";
    }

    @GetMapping("/admin/blog/new-post")
    public String newPostPage(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "new-post";
    }

    @GetMapping("/admin/blog/posts/{id}")
    public String updatePostPage(@PathVariable("id") Long id, Model model) throws IOException {
        PostDetailDTO post = postService.getById(id);
        List<String> categories = categoryService.getAll();

        model.addAttribute("post", post);
        model.addAttribute("categories", categories);

        return "update-post";
    }
}
