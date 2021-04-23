package pooro.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pooro.blog.domain.Category;
import pooro.blog.error.ErrorCode;
import pooro.blog.exception.category.CategoryDuplicateException;
import pooro.blog.repository.CategoryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    @Test
    void 카테고리_생성() {
        //given
        Long categoryId = 1L;
        String name = "test";
        Category category = Category.createCategory(categoryId, name);

        given(categoryRepository.save(any())).willReturn(category);
        given(categoryRepository.findOne(categoryId)).willReturn(Optional.ofNullable(category));

        //when
        Long saveId = categoryService.create(name);

        //then
        Category findCategory = categoryRepository.findOne(saveId).get();
        assertEquals(category.getId(), findCategory.getId(), "저장된 ID 값이 같아야 합니다.");
        assertEquals(category.getName(), findCategory.getName(), "저장된 카테고리 이름이 같아야 합니다.");
    }

    @Test
    void 카테고리_생성_중복_예외() {
        //given
        String name = "test";
        Category category = Category.createCategory("test");
        given(categoryRepository.findByName(name)).willReturn(Optional.ofNullable(category));

        //when
        CategoryDuplicateException thrown =
                assertThrows(CategoryDuplicateException.class, () -> categoryService.create(name));

        //then
        assertEquals(ErrorCode.CATEGORY_DUPLICATE, thrown.getErrorCode(), "카테고리 중복 예외를 발생시켜야 합니다.");
    }
}