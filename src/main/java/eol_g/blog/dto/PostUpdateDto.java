package eol_g.blog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostUpdateDto {

    private String category;
    private String subject;
    private String content;

    /**
     * json request를 PostUploadDto 객체로 변환
     */
    @JsonCreator
    public PostUpdateDto(@JsonProperty("category") String category,
                         @JsonProperty("subject") String subject,
                         @JsonProperty("content") String content) {
        this.category = category;
        this.subject = subject;
        this.content = content;
    }
}
