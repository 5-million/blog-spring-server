package pooro.blog.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pooro.blog.domain.PostStatus;

@Getter
public class PostUploadDto {

    private PostStatus status;
    private String category;
    private String subject;
    private String content;

    private PostUploadDto() {}

    /**
     * json request를 PostUploadDto 객체로 변환
     */
    @JsonCreator
    public PostUploadDto(@JsonProperty("status") PostStatus status,
                         @JsonProperty("category") String category,
                         @JsonProperty("subject") String subject,
                         @JsonProperty("content") String content) {
        this.status = status;
        this.category = category;
        this.subject = subject;
        this.content = content;
    }

    public static PostUploadDto createPostUploadDto(PostStatus status,
                                                    String category,
                                                    String subject,
                                                    String content) {
        PostUploadDto postDto = new PostUploadDto();
        postDto.status = status;
        postDto.category = category;
        postDto.subject = subject;
        postDto.content = content;

        return postDto;
    }
}
