package eol_g.blog.service;

import eol_g.blog.domain.Category;
import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.category.CategoryDuplicateException;
import eol_g.blog.exception.category.CategoryNotExistException;
import eol_g.blog.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private FileService fileService;
    @InjectMocks private CategoryService categoryService;

    @Test
    void create() {
        //given


        //when

        //then
    }

    @Test
    void create_카테고리가_이미_존재하는_경우() {
        //given
        Long id = 1L;
        String name = "category";
        Category testCategory = Category.createCategory(id, name);

        given(categoryRepository.findByName(name)).willReturn(Optional.ofNullable(testCategory));

        //when
        CategoryDuplicateException thrown = assertThrows(CategoryDuplicateException.class, () -> categoryService.create(name));

        //then
        assertEquals(ErrorCode.CATEGORY_DUPLICATE, thrown.getErrorCode());
    }

    @Test
    void getAll() {
        //given
        List<Category> testCategoryList = createTestCategoryList();

        given(categoryRepository.findAll()).willReturn(Optional.ofNullable(testCategoryList));

        //when
        List<String> result = categoryService.getAll();

        //then
        List<String> expected = testCategoryList.stream().map(category -> category.getName()).collect(Collectors.toList());

        assertEquals(expected.getClass(), result.getClass());
        assertEquals(expected.size(), result.size());
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void getAll_카테고리가_존재하지_않을_경우() {
        //given
        given(categoryRepository.findAll()).willReturn(Optional.empty());

        //when
        CategoryNotExistException thrown = assertThrows(CategoryNotExistException.class, () -> categoryService.getAll());

        //then
        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, thrown.getErrorCode());
    }

    private List<Category> createTestCategoryList() {
        List<Category> testCategoryList = new ArrayList<>();

        Category jpa = Category.createCategory(1L, "jpa");
        Category server = Category.createCategory(2L, "server");
        Category spring = Category.createCategory(3L, "spring");

        testCategoryList.add(jpa);
        testCategoryList.add(server);
        testCategoryList.add(spring);

        return testCategoryList;
    }
}