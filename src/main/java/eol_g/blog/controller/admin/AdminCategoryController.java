package eol_g.blog.controller.admin;

import eol_g.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/admin/blog/category/**")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 등록
     */
    @PostMapping("/")
    public String create(@RequestParam("category") String category, RedirectAttributes model) throws IOException {
        categoryService.create(category);

        model.addFlashAttribute("msg", "카테고리 생성 완료");

        return "redirect:/admin/blog";
    }
}
