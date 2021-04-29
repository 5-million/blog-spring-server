package pooro.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pooro.blog.domain.Category;
import pooro.blog.exception.category.CategoryDuplicateException;
import pooro.blog.exception.category.CategoryNotExistException;
import pooro.blog.repository.CategoryRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileService fileService;

    /**
     * 카테고리 생성
     */
    @Transactional
    public Long create(String name) throws CategoryDuplicateException, IOException {
        // 카테고리 중복 검사
        validateDuplicateCategory(name);

        // 카테고리 생성
        Category category = Category.createCategory(name);

        // 카테고리 폴더 생성
        fileService.crateCategoryFolder(name);

        // 카테고리 저장
        return categoryRepository.save(category).getId();
    }

    public List<String> getAll() {
        Optional<List<Category>> optCategories = categoryRepository.findAll();

        if(optCategories.isPresent()) {
            List<Category> categories = optCategories.get();
            List<String> categoryNames = new ArrayList<>();

            for(Category c : categories) {
                categoryNames.add(c.getName());
            }

            return categoryNames;
        }
        else throw new CategoryNotExistException();
    }

    private void validateDuplicateCategory(String name) {
        categoryRepository.findByName(name).ifPresent(c -> {
            throw new CategoryDuplicateException();
        });
    }
}
