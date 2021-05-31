package eol_g.blog.service.post;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.dto.PostUpdateDTO;
import eol_g.blog.dto.PostUploadDTO;
import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.category.CategoryNotExistException;
import eol_g.blog.exception.post.PostDuplicateException;
import eol_g.blog.exception.post.PostNotExistException;
import eol_g.blog.exception.post.PostNotTempException;
import eol_g.blog.repository.CategoryRepository;
import eol_g.blog.repository.PostRepository;
import eol_g.blog.service.AwsS3Service;
import eol_g.blog.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AdminPostServiceTest extends PostServiceTest {

    @Mock private FileService fileService;
    @Mock private PostRepository postRepository;
    @Mock private AwsS3Service awsS3Service;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private AdminPostService postService;

    @Test
    void getAll() {
        //given
        List<Post> testPostList = createTestPostList("category");

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDTO> result = postService.getAll();

        //then
        List<PostListDTO> expected = PostListDTO.toDTO(testPostList);
        assertEquals(expected.getClass(), result.getClass());
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).toString(), result.get(i).toString());
        }
    }

    @Test
    void upload() throws IOException {
        //given
        Long testPostId = 1L;
        PostStatus status = PostStatus.PUBLIC;
        String category = "category";
        String subject = "subject";
        String content = "content";
        String pathName = "posts/" + status.toString().toLowerCase() + "/" + category + "/" + subject + ".md";

        PostUploadDTO testPostUploadDTO = PostUploadDTO.toDTO(status, category, subject, content);
        Post testPost = createTestPost(testPostId, category, subject, status);
        Category testCategory = Category.createCategory(1L, category);
        File testPostFile = new File(pathName);

        given(postRepository.findBySubject(subject)).willReturn(Optional.empty());
        given(categoryRepository.findByName(category)).willReturn(Optional.ofNullable(testCategory));
        given(fileService.createFile(pathName, content)).willReturn(testPostFile);
        given(awsS3Service.upload(pathName, testPostFile)).willReturn(pathName);
        given(postRepository.save(any(Post.class))).willReturn(testPost.getId());

        //when
        Long result = postService.upload(testPostUploadDTO);

        //then
        assertEquals(Long.class, result.getClass());
        assertEquals(testPostId, result);
    }

    @Test
    void upload_제목이_중복되는_경우() {
        //given
        String category = "category";
        String subject = "subject";
        PostStatus status = PostStatus.PUBLIC;
        PostUploadDTO testPostUploadDTO = PostUploadDTO.toDTO(status, category, subject, "content" );
        Post testPost = createTestPost(1L, category, "subject", status);

        given(postRepository.findBySubject(subject)).willReturn(Optional.ofNullable(testPost));

        //when
        PostDuplicateException thrown = assertThrows(PostDuplicateException.class, () -> postService.upload(testPostUploadDTO));

        //then
        assertEquals(ErrorCode.POST_DUPLICATE, thrown.getErrorCode());
    }

    @Test
    void upload_존재하지_않는_카테고리로_업로드할_경우() {
        //given
        String category = "category";
        String subject = "subject";
        PostStatus status = PostStatus.PUBLIC;
        PostUploadDTO testPostUploadDTO = PostUploadDTO.toDTO(status, category, subject, "content");

        given(postRepository.findBySubject(subject)).willReturn(Optional.empty());
        given(categoryRepository.findByName(category)).willReturn(Optional.empty());

        //when
        CategoryNotExistException thrown = assertThrows(CategoryNotExistException.class, () -> postService.upload(testPostUploadDTO));

        //then
        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void update_업로드하려는_포스트가_존재하지_않을_경우() {
        //given
        Long testPostId = 1L;
        PostUpdateDTO testPostUpdateDTO = PostUpdateDTO.toDTO("category", "subject", "content");

        given(postRepository.findById(testPostId)).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.update(testPostId, testPostUpdateDTO));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void update_존재하지_않는_카테고리로_변경할_경우() {
        //given
        Long testPostId = 1L;
        String category = "category";
        Post testPost = createTestPost(testPostId, category, "subject", PostStatus.PUBLIC);
        PostUpdateDTO testPostUpdateDTO = PostUpdateDTO.toDTO(category, "subject", "content");

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));
        given(categoryRepository.findByName(category)).willReturn(Optional.empty());

        //when
        CategoryNotExistException thrown = assertThrows(CategoryNotExistException.class, () -> postService.update(testPostId, testPostUpdateDTO));

        //then
        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void delete_삭제하려는_포스트가_존재하지_않는_경우() {
        //given
        Long testPostId = 1L;
        given(postRepository.findById(testPostId)).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.delete(testPostId));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void convertToPublic_공개하려는_포스트가_존재하지_않는_경우 () {
        //given
        Long testPostId = 1L;
        given(postRepository.findById(testPostId)).willReturn(Optional.empty());

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.delete(testPostId));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void convertToPublic_이미_공개된_포스트인_경우() {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(testPostId, "category", "subject", PostStatus.PUBLIC);

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));

        //when
        PostNotTempException thrown = assertThrows(PostNotTempException.class, () -> postService.convertToPublic(testPostId));

        //then
        assertEquals(ErrorCode.POST_NOT_TEMP, thrown.getErrorCode());
    }

    @Test
    void createPathname() {
        //given
        PostStatus status = PostStatus.PUBLIC;
        String category = "category";
        String subject = "subject subject";

        //when
        String result = postService.createPathname(status, category, subject);

        //then
        String expected = "posts/" + status.toString().toLowerCase() + "/"
                + category + "/"
                + subject.replace(" ", "_")
                + ".md";

        assertEquals(expected, result);
        assertEquals(expected.equals(result), true);
    }
}