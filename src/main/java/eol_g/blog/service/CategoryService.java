package eol_g.blog.service;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.PostStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eol_g.blog.exception.category.CategoryDuplicateException;
import eol_g.blog.exception.category.CategoryNotExistException;
import eol_g.blog.repository.CategoryRepository;

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

        // 카테고리 폴더를 생성할 pathName
        String tempPathName = createFolderPathName(PostStatus.TEMP, name);
        String publicPathName = createFolderPathName(PostStatus.RELEASE, name);

        // 카테고리 폴더 생성
        fileService.createFolder(tempPathName, publicPathName);

        // 카테고리 저장
        return categoryRepository.save(category).getId();
    }

    /**
     * 모든 카테고리 이름을 가져오는 함수
     */
    public List<String> getAll() {
        // 모든 카테고리 엔티티
        List<Category> categoryList = getCategoryEntityList();

        // 카테고리 엔티티로부터 이름만 뽑아 리스트 생성
        List<String> categoryNameList = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNameList.add(category.getName());
        }

        // 카테고리 이름 리스트 반환
        return categoryNameList;
    }

    /**
     * 카테고리 리포지토리로부터 카테고리 엔티티 리스트를 가져오는 함수
     */
    private List<Category> getCategoryEntityList() {
        Optional<List<Category>> optional = categoryRepository.findAll();
        if(!optional.isPresent()) throw new CategoryNotExistException();

        return optional.get();
    }

    /**
     * 카테고리 이름 중복 검사
     */
    private void validateDuplicateCategory(String name) {
        categoryRepository.findByName(name).ifPresent(c -> {
            throw new CategoryDuplicateException();
        });
    }

    /**
     * 생성할 카테고리 폴더의 pathName을 생성
     */
    private String createFolderPathName(PostStatus status, String name) {
        return "posts/" + status.toString().toLowerCase() + "/" + name;
    }
}
