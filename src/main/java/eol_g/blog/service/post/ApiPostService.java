package eol_g.blog.service.post;

import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.exception.post.PostNotExistException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApiPostService extends AbstractPostService {

    @Override
    public List<PostListDTO> getAll() {
        // 공개된 포스트 엔티티 리스트 가져오기
        List<Post> postList = getPostEntityList().stream()
                .filter(post -> post.getStatus() == PostStatus.PUBLIC)
                .collect(Collectors.toList());

        // 공개된 포스트가 존재 여부 검증
        if(postList.size() == 0) throw new PostNotExistException();

        return PostListDTO.toDTO(postList);
    }

    @Override
    public PostDetailDTO getById(Long id) throws IOException {
        // 포스트 엔티티
        Post post = getPostEntityById(id);

        // 공개되지 않은 포스트에 접근할 경우 포스트를 감춤
        if (post.getStatus() == PostStatus.TEMP) throw new PostNotExistException();

        // 포스트 내용 가져오기
        String content = getContent(post);

        // PostDetailDTO 생성
        return PostDetailDTO.toDTO(post, content);
    }
}
