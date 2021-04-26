package pooro.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pooro.blog.domain.Category;
import pooro.blog.domain.PostStatus;

import java.time.LocalDate;

@ToString
@Getter
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
}
