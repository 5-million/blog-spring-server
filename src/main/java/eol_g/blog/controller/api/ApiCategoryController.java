package eol_g.blog.controller.api;

import eol_g.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blog/category/**")
@RequiredArgsConstructor
public class ApiCategoryController {

    private final CategoryService categoryService;

    /**
     * 모든 카테고리 가져오기
     */
    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAll() {
        return categoryService.getAll();
    }
}
