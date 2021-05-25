package eol_g.blog.dto;

import eol_g.blog.domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import eol_g.blog.domain.Category;
import eol_g.blog.domain.PostStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class PostListDto {
    private Long id;
    private String category;
    private String subject;
    private LocalDate uploadDate;
    private String status;

    @Builder
    private PostListDto(Long id, Category category, String subject, LocalDate uploadDate, PostStatus status) {
        this.id = id;
        this.category = category.getName();
        this.subject = subject;
        this.uploadDate = uploadDate;
        this.status = status.toString();
    }

    public static List<PostListDto> toDTOList(List<Post> posts) {
        List<PostListDto> entityList = new ArrayList<>();

        for (Post post : posts) {
            PostListDto dto = PostListDto.builder().id(post.getId())
                    .category(post.getCategory())
                    .subject(post.getSubject())
                    .uploadDate(post.getUploadDate())
                    .status(post.getStatus())
                    .build();
            entityList.add(dto);
        }

        return entityList;
    }
}
