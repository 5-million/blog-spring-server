package eol_g.blog.service.post;

import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.error.ErrorCode;
import eol_g.blog.exception.post.PostNotExistException;
import eol_g.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApiPostServiceTest extends PostServiceTest {

    @Mock private PostRepository postRepository;
    @InjectMocks private ApiPostService postService;

    @Test
    void getAll() {
        //given
        List<Post> testPostList = createTestPostList("category");

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        List<PostListDTO> result = postService.getAll();

        //then
        testPostList = testPostList.stream().filter(post -> post.getStatus() == PostStatus.RELEASE).collect(Collectors.toList());
        List<PostListDTO> expected = PostListDTO.toDTO(testPostList);

        assertEquals(expected.getClass(), result.getClass());
        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(result.get(i).getStatus(), PostStatus.RELEASE.toString());
            assertEquals(expected.get(i).toString(), result.get(i).toString());
        }
    }

    @Test
    void getAll_공개된_포스트_엔티티가_없는_경우() {
        //given
        List<Post> testPostList =
                createTestPostList("category").stream().filter(post -> post.getStatus() == PostStatus.TEMP).collect(Collectors.toList());

        given(postRepository.findAll()).willReturn(Optional.ofNullable(testPostList));

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getAll());

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }

    @Test
    void getById_공개된_포스트가_아닌_경우() {
        //given
        Post testPost = createTestPost(1L, "category", "subject", PostStatus.TEMP);

        given(postRepository.findById(1L)).willReturn(Optional.ofNullable(testPost));

        //when
        PostNotExistException thrown = assertThrows(PostNotExistException.class, () -> postService.getById(1L));

        //then
        assertEquals(ErrorCode.POST_NOT_EXIST, thrown.getErrorCode());
    }
}