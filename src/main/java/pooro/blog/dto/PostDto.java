package pooro.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pooro.blog.domain.Category;

import java.time.LocalDate;

@Getter
@ToString
public class PostDto {

    private Long id;
    private String category;
    private String subject;
    private String content;
    private LocalDate uploadDate;

    @Builder
    private PostDto(Long id, Category category, String subject, String content, LocalDate uploadDate) {
        this.id = id;
        this.category = category.getName();
        this.subject = subject;
        this.content = content;
        this.uploadDate = uploadDate;
    }
}
