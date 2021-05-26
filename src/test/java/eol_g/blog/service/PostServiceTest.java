package eol_g.blog.service;

import eol_g.blog.dto.AdminPostDetailDTO;
import eol_g.blog.dto.ApiPostDetailDTO;
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
    void getAllForAdmin() {
        //given
        List<Post> testPostList = createTestPostList("category");

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDto> result = postService.getAllForAdmin();

        //then
        List<PostListDto> expected = PostListDto.toDTOList(testPostList);

        assertEquals(expected.size(), result.size(), "개수가 정확해야 합니다.");
        for (int idx = 0; idx < expected.size(); idx++) {
            assertEquals(expected.get(idx).toString(), result.get(idx).toString(), "각 객체에 저장된 값이 정확해야합니다.");
        }
    }

    @Test
    void getAllForAdmin_NotExistException() {
        //given
        given(postRepository.findAll()).willReturn(Optional.empty());

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getAllForAdmin());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야합니다.");
    }

    @Test
    void getAllForApi() {
        //given
        List<Post> testPostList = createTestPostList("category");

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDto> result = postService.getAllForApi();

        //then
        List<PostListDto> expected = new ArrayList<>();
        for (Post post : testPostList) {
            if (post.getStatus() == PostStatus.TEMP)
                continue;

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
    void getAllForApi_NotExistException() {
        //given
        given(postRepository.findAll()).willReturn(Optional.empty());

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getAllForApi());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야 합니다.");
    }

    @Test
    void getByIdForAdmin() throws IOException {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(testPostId, "category", PostStatus.PUBLIC);
        String content = "content";

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willReturn(content);

        //when
        AdminPostDetailDTO result = postService.getByIdForAdmin(testPostId);

        //then
        AdminPostDetailDTO expected = AdminPostDetailDTO.toDTO(testPost, content);

        assertEquals(AdminPostDetailDTO.class, result.getClass(), "AdminPostDetailDTO가 반환되어야 합니다.");
        assertEquals(expected.toString(), result.toString(), "반환되는 값이 정확해야 합니다.");
    }

    @Test
    void getByIdForAdmin_NotExistException() {
        //given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getByIdForAdmin(1L));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_FOUND 예외를 던져야합니다.");
    }

    @Test
    void getByIdForAdmin_NotFoundException() throws IOException {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(testPostId, "category", PostStatus.PUBLIC);

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willThrow(new PostNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willThrow(new PostNotFoundException());

        //when
        PostNotFoundException thrown =
                assertThrows(PostNotFoundException.class, () -> postService.getByIdForAdmin(testPostId));

        //then
        assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야합니다.");
    }

    @Test
    void getByIdForApi() throws IOException {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(1L, "category", PostStatus.PUBLIC);
        String content = "test content";

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willReturn(content);

        //when
        ApiPostDetailDTO result = postService.getByIdForApi(testPostId);

        //then
        ApiPostDetailDTO expected = ApiPostDetailDTO.toDTO(testPost, content) ;

        assertEquals(expected.toString(), result.toString(), "PostDetailDTO가 정확히 생성되어야 합니다.");
    }

    @Test
    void getByIdForApi_로컬에_파일이_존재하지_않는_경우() throws IOException {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(testPostId, "category", PostStatus.PUBLIC);
        String content = "content";

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willThrow(new PostNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willReturn(content);

        //when
        ApiPostDetailDTO result = postService.getByIdForApi(testPostId);

        //then
        ApiPostDetailDTO expected = ApiPostDetailDTO.toDTO(testPost, content);

        assertEquals(expected.toString(), result.toString(), "PostDetailDTO가 정확히 생성되어야 합니다.");
    }

    @Test
    void getByIdForApi_NotFoundException() throws IOException {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(testPostId, "category", PostStatus.PUBLIC);
        String content = "content";

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));
        given(fileService.getContent(testPost.getFilePath())).willThrow(new PostNotFoundException());
        given(awsS3Service.getObjectContent(testPost.getS3Key())).willThrow(new PostNotFoundException());

        //when
        PostNotFoundException thrown =
                assertThrows(PostNotFoundException.class, () -> postService.getByIdForApi(testPostId));

        //then
        assertEquals(
                ErrorCode.POST_NOT_FOUND,
                thrown.getErrorCode(),
                "로컬과 s3 모두에 포스트 파일이 존재하지 않을 경우 POST_NOT_FOUND 예외를 던져야합니다.");
    }

    @Test
    void getByIdForApi_포스트가_임시저장상태인_경우() {
        //given
        Long testPostId = 1L;
        Post testPost = createTestPost(testPostId, "category", PostStatus.TEMP);

        given(postRepository.findById(testPostId)).willReturn(Optional.ofNullable(testPost));

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getByIdForApi(testPostId));

        //then
        assertEquals(
                ErrorCode.POST_NOT_EXIST,
                thrown.getErrorCode(),
                "API 요청에서 포스트가 임시저장상태인 경우 POST_NOT_EXIST 예외를 던져 포스트를 감춰야합니다.");
    }

    @Test
    void getByIdForApi_NotExistException() {
        //given
        Long testPostId = 1L;
        given(postRepository.findById(testPostId)).willReturn(Optional.empty());

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getByIdForApi(testPostId));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야합니다.");
    }

    @Test
    void getByCategory() {
        //given
        String category = "jpa";
        List<Post> testPostList = createTestPostList(category);

        given(postRepository.findByCategory(category)).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDto> result = postService.getByCategory(category);

        //then
        List<PostListDto> expected = PostListDto.toDTOList(testPostList);

        assertEquals(expected.size(), result.size(), "사이즈가 정확해야 합니다.");
        for (int index = 0; index < expected.size(); index++) {
            assertEquals(
                    expected.get(index).toString(),
                    result.get(index).toString(),
                    "각 데이터가 정확해야 합ㄴ디ㅏ."
            );
        }
    }

    @Test
    void getByCategory_NotExistException() {
        //given
        String category = "category";
        given(postRepository.findByCategory(category)).willReturn(Optional.empty());

        //when
        PostNotExistException thrown =
                assertThrows(PostNotExistException.class, () -> postService.getByCategory(category));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode(), "POST_NOT_EXIST 예외를 던져야 합니다.");
    }

    @Test
    void 포스트_업로드() throws IOException {
        //given
        String category = "category";
        String pathname = "posts/public/" + category + "/test_subject.md";
        Post testPost = createTestPost(1L, category, PostStatus.PUBLIC);

        File uploadFile = new File(pathname);
        Category testCategory = Category.createCategory(1L, category);
        PostUploadDto postUploadDto = createPostUploadDtoRequest(category);

        given(postRepository.findBySubject(anyString())).willReturn(Optional.empty());
        given(categoryRepository.findByName(anyString())).willReturn(Optional.ofNullable(testCategory));
        given(fileService.createFile(anyString(), anyString())).willReturn(uploadFile);
        given(awsS3Service.upload(anyString(), any(File.class))).willReturn(pathname);
        given(postRepository.save(any(Post.class))).willReturn(testPost.getId());

        //when
        Long postId = postService.upload(postUploadDto);

        //then
        assertEquals(testPost.getId(), postId, "반환되는 포스트의 id값이 정확해야 합니다.");
    }

    @Test
    void 포스트_중복_예외() {
        //given
        String category = "category";
        PostUploadDto postUploadDto = createPostUploadDtoRequest(category);
        Post testPost = createTestPost(1L, category, PostStatus.PUBLIC);

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
        PostUploadDto testPostUpladDto = createPostUploadDtoRequest(category);

        given(categoryRepository.findByName(anyString())).willReturn(Optional.empty());

        //when
        CategoryNotFoundException thrown =
                assertThrows(CategoryNotFoundException.class, () -> postService.upload(testPostUpladDto));

        //then
        assertEquals(ErrorCode.CATEGORY_NOT_FOUND,
                thrown.getErrorCode(),
                "CATEGORY_NOT_FOUND 예외를 던져야 합니다.");
    }

    private PostUploadDto createPostUploadDtoRequest(String category) {
        PostStatus status = PostStatus.PUBLIC;
        String subject = "test subject";
        String content = "test content";

        return PostUploadDto.createPostUploadDto(status, category, subject, content);
    }

    private Post createTestPost(Long id, String category, PostStatus status) {
        String createId = id.toString();
        String filePathAndKey = "posts/" + status.toString().toLowerCase() + "/"
                + category + "/test_subject" + createId.toString() + ".md";

        return Post.builder()
                .id(id)
                .category(Category.createCategory(id, category))
                .subject("test_subject" + createId.toString())
                .filePath(filePathAndKey)
                .s3Key(filePathAndKey)
                .status(status)
                .build();
    }

    private List<Post> createTestPostList(String category) {
        List<Post> testPostList = new ArrayList<>();

        for (Long i = 1L; i < 100; i++) {
            PostStatus status = PostStatus.PUBLIC;
            if (i % 2 == 0) status = PostStatus.TEMP;

            testPostList.add(createTestPost(i, category, status));
        }

        return testPostList;
    }
}