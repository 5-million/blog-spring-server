package eol_g.blog.controller.admin;

import eol_g.blog.service.post.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin/blog/**")
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminPostService postService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("posts", postService.getAll());
        return "index";
    }

    @GetMapping("/new-post")
    public String newPostPage() {
        return "new-post";
    }
}
