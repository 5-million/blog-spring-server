package pooro.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pooro.blog.exception.category.CategoryDuplicateException;
import pooro.blog.service.CategoryService;

import java.util.Map;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 생성 컨트롤러
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody Map<String, String> body) throws CategoryDuplicateException {
        categoryService.create(body.get("name"));
    }
}
