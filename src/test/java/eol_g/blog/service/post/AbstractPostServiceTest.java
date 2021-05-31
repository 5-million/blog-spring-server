package eol_g.blog.service.post;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.category.CategoryNotExistException;
import eol_g.blog.exception.post.PostNotExistException;
import eol_g.blog.exception.post.PostNotFoundException;
import eol_g.blog.repository.CategoryRepository;
import eol_g.blog.repository.PostRepository;
import eol_g.blog.service.AwsS3Service;
import eol_g.blog.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AbstractPostServiceTest extends PostServiceTest {

    /**
     * Api / Admin 공통으로 테스트할 부분
     */

    @Mock private FileService fileService;
    @Mock private PostRepository postRepository;
    @Mock private AwsS3Service awsS3Service;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private AdminPostService postService;

    @Test
    void getAll_포스트_엔티티가_존재하지_않는_경우() {
        //given
        given(postRepository.findAll()).willReturn(Optional.ofNullable(null));

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getAll());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void getById() throws IOException {
        //given
        Long id = 1L;
        String content = "content";
        Post testPost = createTestPost(id, "category", "test_subject", PostStatus.PUBLIC);

        given(postRepository.findById(id)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willReturn(content);

        //when
        PostDetailDTO result = postService.getById(id);

        //then
        PostDetailDTO expected = PostDetailDTO.toDTO(testPost, content);

        assertEquals(expected.getClass(), result.getClass());
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void getById_존재하지_않는_포스트일_경우() {
        //given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getById(anyLong()));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void getById_로컬에서_포스트_파일을_찾을_수_없는_경우() throws IOException {
        //given
        Long id = 1L;
        String content = "content";
        Post testPost = createTestPost(id, "category", "subject", PostStatus.TEMP);

        given(postRepository.findById(id)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willThrow(new FileNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willReturn(content);

        //when
        PostDetailDTO result = postService.getById(id);

        //then
        PostDetailDTO expected = PostDetailDTO.toDTO(testPost, content);

        assertEquals(expected.getClass(), result.getClass());
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void getById_로컬과_s3에서_포스트_파일을_찾을_수_없는_경우 () throws IOException {
        //given
        Long id = 1L;
        Post testPost = createTestPost(id, "category", "subject", PostStatus.PUBLIC);

        given(postRepository.findById(id)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willThrow(new FileNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willThrow(new PostNotFoundException());

        //when
        PostNotFoundException thrown = assertThrows(PostNotFoundException.class, () -> postService.getById(id));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode());
    }

    @Test
    void getByCategory() {
        //given
        String categoryName = "category";
        List<Post> testPostList = createTestPostList(categoryName);
        testPostList = testPostList.stream()
                .filter((post) -> post.getCategory().equals(categoryName))
                .collect(Collectors.toList());

        given(postRepository.findByCategory(categoryName)).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDTO> result = postService.getByCategory(categoryName);

        //then
        List<PostListDTO> expected = PostListDTO.toDTO(testPostList);

        assertEquals(expected.getClass(), result.getClass());
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), result.get(i).toString());
        }
    }

    @Test
    void getByCategory_카테고리에_해당하는_포스트가_없을_경우() {
        //given
        given(postRepository.findByCategory(anyString())).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getByCategory(anyString()));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void getPostEntityById() {
        //given
        Long id = 1L;
        Post testPost = createTestPost(id, "category", "subject", PostStatus.PUBLIC);

        given(postRepository.findById(id)).willReturn(Optional.ofNullable(testPost));

        //when
        Post result = postService.getPostEntityById(id);

        //then
        assertEquals(testPost.getClass(), result.getClass());
        assertEquals(testPost.toString(), result.toString());
    }

    @Test
    void getPostEntityById_id에_해당하는_포스트가_없을_경우() {
        //given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getPostEntityById(anyLong()));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }


    @Test
    void getPostEntityList() {
        //given
        String categoryName = "category";
        List<Post> testPostList = createTestPostList(categoryName);

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<Post> result = postService.getPostEntityList();

        //then
        assertEquals(testPostList.getClass(), result.getClass());
        assertEquals(testPostList.size(), result.size());
        for (int id = 1; id < testPostList.size(); id++) {
            assertEquals(testPostList.get(id).toString(), result.get(id).toString());
        }
    }

    @Test
    void getPostEntityList_포스트_엔티티가_존재하지_않는_경우() {
        //given
        given(postRepository.findAll()).willReturn(Optional.ofNullable(null));

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getPostEntityList());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void getPostEntityListByCategory() {
        //given
        String categoryName = "category";
        List<Post> testPostList = createTestPostList(categoryName);
        testPostList = testPostList.stream().filter((post) -> post.getCategory().equals(categoryName)).collect(Collectors.toList());

        given(postRepository.findByCategory(categoryName)).willReturn(Optional.ofNullable(testPostList));

        //when
        List<Post> result = postService.getPostEntityListByCategory(categoryName);

        //then
        assertEquals(testPostList.getClass(), result.getClass());
        assertEquals(testPostList.size(), result.size());
        for (int id = 0; id < testPostList.size(); id++) {
            assertEquals(testPostList.get(id).toString(), result.get(id).toString());
        }
    }

    @Test
    void getPostEntityListByCategory_카테고리에_해당하는_포스트_엔티티가_없을_경우() {
        //given
        given(postRepository.findByCategory(anyString())).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getPostEntityListByCategory(anyString()));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void getContent() throws IOException {
        //given
        String content = "content";
        String pathName = createObjectKey(PostStatus.PUBLIC, "category", "subject");

        given(fileService.getContent(pathName)).willReturn(content);

        //when
        String result = fileService.getContent(pathName);

        //then
        assertEquals(content, result);
    }

    @Test
    void getContent_로컬에_파일이_존재하지_않는_경우() throws IOException {
        //given
        String content = "content";
        Post testPost = createTestPost(1L, "category", "subject", PostStatus.PUBLIC);

        given(fileService.getContent(testPost.getFilePath())).willThrow(new FileNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willReturn(content);

        //when
        String result = postService.getContent(testPost);

        //then
        assertEquals(content, result);
    }

    @Test
    void getContent_로컬과_s3에_파일이_존재하지_않는_경우() throws IOException {
        //given
        Post testPost = createTestPost(1L, "category", "subject", PostStatus.TEMP);

        given(fileService.getContent(testPost.getFilePath())).willThrow(new FileNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willThrow(new PostNotFoundException());

        //when
        PostNotFoundException thrown = assertThrows(PostNotFoundException.class, () -> postService.getContent(testPost));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode());
    }

    @Test
    void getCategoryEntityByName() {
        //given
        String categoryName = "category";
        Category testCategory = Category.createCategory(categoryName);
        given(categoryRepository.findByName(categoryName)).willReturn(Optional.ofNullable(testCategory));

        //when
        Category result = postService.getCategoryEntityByName(categoryName);

        //then
        assertEquals(testCategory.getClass(), result.getClass());
        assertEquals(testCategory.getName(), result.getName());
    }

    @Test
    void getCategoryEntityByName_카테고리_이름에_해당하는_카테고리_엔티티가_존재하지_않는_경우() {
        //given
        given(categoryRepository.findByName(anyString())).willReturn(Optional.empty());

        //when
        CategoryNotExistException thrown = assertThrows(CategoryNotExistException.class, () -> postService.getCategoryEntityByName(anyString()));

        //then
        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, thrown.getErrorCode());
    }
}