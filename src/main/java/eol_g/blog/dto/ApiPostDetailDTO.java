package eol_g.blog.dto;

import eol_g.blog.domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import eol_g.blog.domain.Category;

import java.time.LocalDate;

@Getter
@ToString
public class ApiPostDetailDTO {

    private Long id;
    private String category;
    private String subject;
    private String content;
    private LocalDate uploadDate;

    @Builder
    private ApiPostDetailDTO(Long id, Category category, String subject, String content, LocalDate uploadDate) {
        this.id = id;
        this.category = category.getName();
        this.subject = subject;
        this.content = content;
        this.uploadDate = uploadDate;
    }

    public static ApiPostDetailDTO toDTO(Post post, String content) {
        return ApiPostDetailDTO.builder()
                .id(post.getId())
                .category(post.getCategory())
                .subject(post.getSubject())
                .content(content)
                .uploadDate(post.getUploadDate())
                .build();
    }
}
