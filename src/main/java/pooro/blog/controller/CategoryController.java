package pooro.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pooro.blog.domain.Category;
import pooro.blog.exception.category.CategoryDuplicateException;
import pooro.blog.service.CategoryService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 모든 카테고리 전송
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAll() {
       return categoryService.getAll();
    }

    /**
     * 카테고리 생성 컨트롤러
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody Map<String, String> body) throws CategoryDuplicateException, IOException {
        categoryService.create(body.get("name"));
    }
}
