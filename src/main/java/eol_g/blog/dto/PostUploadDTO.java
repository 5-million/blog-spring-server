package eol_g.blog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import eol_g.blog.domain.PostStatus;

@Getter
public class PostUploadDTO {

    private PostStatus status;
    private String category;
    private String subject;
    private String content;

    private PostUploadDTO() {}

    /**
     * json request를 PostUploadDto 객체로 변환
     */
    @JsonCreator
    public PostUploadDTO(@JsonProperty("status") PostStatus status,
                         @JsonProperty("category") String category,
                         @JsonProperty("subject") String subject,
                         @JsonProperty("content") String content) {
        this.status = status;
        this.category = category;
        this.subject = subject;
        this.content = content;
    }

    public static PostUploadDTO toDTO(PostStatus status,
                                      String category,
                                      String subject,
                                      String content) {
        PostUploadDTO postDto = new PostUploadDTO();
        postDto.status = status;
        postDto.category = category;
        postDto.subject = subject;
        postDto.content = content;

        return postDto;
    }
}
