package eol_g.blog.domain;

import eol_g.blog.exception.post.PostNotTempException;
import lombok.*;

import javax.persistence.*;

import java.time.LocalDate;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String objectKey;

    private LocalDate uploadDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    //== 생성자 로직 ==//
    @Builder
    private Post(Long id, Category category, String subject, String objectKey, PostStatus status) {
        this.id = id;
        this.category = category;
        this.subject = subject;
        this.objectKey = objectKey;
        this.uploadDate = LocalDate.now();
        this.status = status;
    }

    //== 비즈니스 로직 ==//
    public void update(Category newCategory,
                       String newSubject) {
        updateCategory(newCategory);
        updateSubject(newSubject);
        updateObjectKey();
    }

    private void updateCategory(Category newCategory) {
        this.category = newCategory;
    }

    private void updateSubject(String newSubject) {
        this.subject = newSubject;
    }

    private void updateObjectKey() {
        this.objectKey = PostObjectKey.builder()
                .category(this.category)
                .subject(this.subject)
                .status(this.status)
                .build()
                .make();
    }

    public void release() {
        if (this.status == PostStatus.RELEASE)
            throw new PostNotTempException();

        updateStatus(PostStatus.RELEASE);
        updateObjectKey();
    }

    private void updateStatus(PostStatus newStatus) {
        this.status = newStatus;
    }
}
