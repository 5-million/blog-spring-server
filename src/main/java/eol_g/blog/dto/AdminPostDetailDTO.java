package eol_g.blog.dto;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class AdminPostDetailDTO {

    private Long id;
    private String category;
    private String subject;
    private String content;
    private String status;
    private LocalDate uploadDate;

    @Builder
    public AdminPostDetailDTO(Long id,
                              Category category,
                              String subject,
                              String content,
                              PostStatus status,
                              LocalDate uploadDate) {
        this.id = id;
        this.category = category.getName();
        this.subject = subject;
        this.content = content;
        this.status = status.toString();
        this.uploadDate = uploadDate;
    }

    public static AdminPostDetailDTO toDTO(Post post, String content) {
        return AdminPostDetailDTO.builder()
                .id(post.getId())
                .category(post.getCategory())
                .subject(post.getSubject())
                .content(content)
                .status(post.getStatus())
                .uploadDate(post.getUploadDate())
                .build();
    }
}
