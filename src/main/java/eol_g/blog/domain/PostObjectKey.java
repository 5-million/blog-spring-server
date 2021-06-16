package eol_g.blog.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostObjectKey {
    private PostStatus status;
    private Category category;
    private String subject;

    //== 생성자 로직 ==//
    @Builder
    public PostObjectKey(PostStatus status, Category category, String subject) {
        this.status = status;
        this.category = category;
        this.subject = subject;
    }

    //== 비지니스 로직 ==//
    public String make() {
        return "posts/" + status.toString().toLowerCase() + "/" + category.getName() + "/" + subject + ".md";
    }
}
