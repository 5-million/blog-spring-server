package eol_g.blog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PostUpdateDTO {

    private String category;
    private String subject;
    private String content;

    public PostUpdateDTO() {
    }

    /**
     * json request를 PostUploadDto 객체로 변환
     */
    @JsonCreator
    public PostUpdateDTO(@JsonProperty("category") String category,
                         @JsonProperty("subject") String subject,
                         @JsonProperty("content") String content) {
        this.category = category;
        this.subject = subject;
        this.content = content;
    }



    public static PostUpdateDTO toDTO(String category, String subject, String content) {
        PostUpdateDTO postUpdateDto = new PostUpdateDTO();

        postUpdateDto.category = category;
        postUpdateDto.subject = subject;
        postUpdateDto.content = content;

        return postUpdateDto;
    }
}
