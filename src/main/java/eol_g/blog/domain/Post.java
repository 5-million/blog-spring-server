package eol_g.blog.domain;

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
    private String filePath;

    @Column(nullable = false)
    private String s3Key;

    private LocalDate uploadDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    //== 생성자 로직 ==//
    @Builder
    private Post(Long id, Category category, String subject, String filePath, String s3Key, PostStatus status) {
        this.id = id;
        this.category = category;
        this.subject = subject;
        this.filePath = filePath;
        this.s3Key = s3Key;
        this.uploadDate = LocalDate.now();
        this.status = status;
    }

    //== 비즈니스 로직 ==//
    public void updateCategory(Category newCategory) {
        category = newCategory;
    }

    public void updateSubject(String newSubject) {
        subject = newSubject;
    }

    public void updateFilePath(String newFilePath) {
        filePath = newFilePath;
    }

    public void updateS3Key(String newS3Key) {
        s3Key = newS3Key;
    }

    public void update(Category newCategory,
                       String newSubject,
                       String newFilePath,
                       String newS3Key) {
        updateCategory(newCategory);
        updateSubject(newSubject);
        updateFilePath(newFilePath);
        updateS3Key(newS3Key);
    }
}
