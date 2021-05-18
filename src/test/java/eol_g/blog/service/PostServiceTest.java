package eol_g.blog.service;

import eol_g.blog.dto.PostDto;
import eol_g.blog.dto.PostListDto;
import eol_g.blog.dto.PostUploadDto;
import eol_g.blog.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.exception.category.CategoryNotFoundException;
import eol_g.blog.exception.post.PostNotExistException;
import eol_g.blog.exception.post.PostNotFoundException;
import eol_g.blog.exception.post.PostDuplicateException;
import eol_g.blog.repository.CategoryRepository;
import eol_g.blog.repository.PostRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private FileService fileService;
    @Mock private AwsS3Service awsS3Service;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PostRepository postRepository;
    @InjectMocks private PostService postService;

    @Test
    void 포스트_업로드() throws IOException {
        //given
        String category = "category";
        String pathname = "posts/public/" + category + "/test_subject.md";
        Post uploadPost = createTestPost(1L, category);

        File uploadFile = new File(pathname);
        Category testCategory = Category.createCategory(1L, category);
        PostUploadDto postUploadDto = createPostUploadDtoRequest(category);

        given(postRepository.findBySubject(anyString())).willReturn(Optional.ofNullable(null));
        given(categoryRepository.findByName(anyString())).willReturn(Optional.ofNullable(testCategory));
        given(fileService.createPost(anyString(), anyString())).willReturn(uploadFile);
        given(awsS3Service.upload(anyString(), any(File.class))).willReturn(pathname);
        given(postRepository.save(any(Post.class))).willReturn(uploadPost.getId());

        //when
        Long postId = postService.upload(postUploadDto);

        //then
        assertEquals(uploadPost.getId(), postId, "반환되는 포스트의 id값이 정확해야 합니다.");
    }

    @Test
    void 포스트_중복_예외() {
        //given
        String category = "category";
        PostUploadDto postUploadDto = createPostUploadDtoRequest(category);
        Post testPost = createTestPost(1L, category);

        given(postRepository.findBySubject(anyString())).willReturn(Optional.ofNullable(testPost));

        //when
        PostDuplicateException thrown = assertThrows(PostDuplicateException.class, () -> postService.upload(postUploadDto));

        //then
        assertEquals(ErrorCode.POST_DUPLICATE, thrown.getErrorCode(), "포스트 중복 예외를 던져야 합니다.");
    }

    @Test
    void 포스트_업로드_존재하지않는_카테고리_예외() {
        //given
        String category = "category";
        PostUploadDto postUploadDto = createPostUploadDtoRequest(category);

        given(categoryRepository.findByName(anyString())).willReturn(Optional.ofNullable(null));

        //when
        CategoryNotFoundException thrown =
                assertThrows(CategoryNotFoundException.class, () -> postService.upload(postUploadDto));

        //then
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND,
                thrown.getErrorCode(),
                "CATEGORY_NOT_FOUND 예외를 던져야 합니다.");
    }

    @Test
    void getById() throws IOException {
        //given
        Long postId = 1L;
        Post testPost = createTestPost(1L, "category");
        String content = "test content";

        given(postRepository.findOne(any())).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(any(), any())).willReturn(content);

        //when
        PostDto testPostDto = postService.getById(postId);

        //then
        PostDto expectPostDto = PostDto.builder()
                .id(testPost.getId())
                .category(testPost.getCategory())
                .subject(testPost.getSubject())
                .content(content)
                .uploadDate(testPost.getUploadDate())
                .build();

        assertEquals(expectPostDto.toString(), testPostDto.toString(), "PostDto가 정확히 생성되어야 합니다.");
    }

    @Test
    void getById_NotExistException() {
        //given
        Long postId = 1L;
        given(postRepository.findOne(anyLong())).willReturn(Optional.ofNullable(null));

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getById(postId));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야합니다.");
    }

    @Test
    void getById_NotFoundException() throws IOException {
        //given
        Long postId = 1L;
        String category = "spring";
        Post testPost = createTestPost(postId, category);

        given(postRepository.findOne(anyLong())).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(anyString(), anyString())).willThrow(new PostNotFoundException());

        //when
        PostNotFoundException thrown =
                assertThrows(PostNotFoundException.class, () -> postService.getById(postId));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode(), "POST_NOT_FOUND 예외를 던져야합니다.");
    }

    @Test
    void getAll() {
        //given
        List<Post> testPostList = new ArrayList<>();
        for (Long id = 1L; id <= 100; id++) {
            testPostList.add(createTestPost(id, "category"));
        }

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDto> allPost = postService.getAll();

        //then
        List<PostListDto> testAllPost = new ArrayList<>();
        for (Post p : testPostList) {
            PostListDto postListDto = PostListDto.builder()
                    .id(p.getId())
                    .category(p.getCategory())
                    .subject(p.getSubject())
                    .uploadDate(p.getUploadDate())
                    .status(p.getStatus())
                    .build();

            testAllPost.add(postListDto);
        }

        assertEquals(testAllPost.size(), allPost.size(), "개수가 정확해야 합니다.");
        for (int index = 0; index < testAllPost.size(); index++) {
            assertEquals(testAllPost.get(index).toString(), allPost.get(index).toString(), "각 객체에 저장된 값이 정확해야합니다.");
        }
    }

    @Test
    void getAll_NotExistException() {
        //given
        given(postRepository.findAll()).willReturn(Optional.ofNullable(null));

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getAll());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야합니다.");
    }

    @Test
    void getPublicPosts() {
        //given
        List<Post> testPostList = new ArrayList<>();
        String category = "spring";
        for (Long id = 0L; id < 50; id++) {
            Post testPost = createTestPost(id, category);
            testPostList.add(testPost);
        }

        given(postRepository.findPublicPosts()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDto> result = postService.getPublicPosts();

        //then
        List<PostListDto> expected = new ArrayList<>();
        for (Post post : testPostList) {
            PostListDto dto = PostListDto.builder()
                    .id(post.getId())
                    .category(post.getCategory())
                    .subject(post.getSubject())
                    .uploadDate(post.getUploadDate())
                    .status(post.getStatus())
                    .build();
            expected.add(dto);
        }

        assertEquals(expected.size(), result.size(), "리스트의 크기가 동일해야합니다.");
        for (int idx = 0; idx < expected.size(); idx++) {
            assertEquals(expected.get(idx).toString(), result.get(idx).toString(), "각 원소의 내용이 같아야 합니다.");
        }
    }

    @Test
    void getPublicPosts_NotExistException() {
        //given
        given(postRepository.findPublicPosts()).willReturn(Optional.empty());

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getPublicPosts());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야 합니다.");
    }

    @Test
    void getByCategory() {
        //given
        List<Post> testPostList = new ArrayList<>();
        String category = "jpa";
        for (Long id = 0L; id < 50; id++) {
            Post testPost = createTestPost(id, category);
            testPostList.add(testPost);
        }

        given(postRepository.findByCategory(anyString())).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDto> postListByCategory = postService.getByCategory(category);

        //then
        List<PostListDto> expectedDtoList = new ArrayList<>();
        for (Post post : testPostList) {
            PostListDto dto = PostListDto.builder()
                    .id(post.getId())
                    .category(post.getCategory())
                    .subject(post.getSubject())
                    .uploadDate(post.getUploadDate())
                    .status(post.getStatus())
                    .build();
            expectedDtoList.add(dto);
        }

        assertEquals(expectedDtoList.size(), postListByCategory.size(), "사이즈가 정확해야 합니다.");
        for (int index = 0; index < expectedDtoList.size(); index++) {
            assertEquals(
                    expectedDtoList.get(index).toString(),
                    postListByCategory.get(index).toString(),
                    "각 데이터가 정확해야 합ㄴ디ㅏ."
            );
        }
    }

    @Test
    void getByCategory_NotExistException() {
        //given
        given(postRepository.findByCategory(anyString())).willReturn(Optional.ofNullable(null));

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getByCategory("jpa"));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야 합니다.");
    }

    private PostUploadDto createPostUploadDtoRequest(String category) {
        PostStatus status = PostStatus.PUBLIC;
        String subject = "test subject";
        String content = "test content";

        return PostUploadDto.createPostUploadDto(status, category, subject, content);
    }

    private Post createTestPost(Long id, String category) {
        String createId = id.toString();
        String filePathAndKey = "posts/public/" + category + "/test_subject" + createId.toString() + ".md";
        return Post.builder()
                .id(id)
                .category(Category.createCategory(id, category))
                .subject("test_subject" + createId.toString())
                .filePath(filePathAndKey)
                .s3Key(filePathAndKey)
                .status(PostStatus.PUBLIC)
                .build();
    }
}