package pooro.blog.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;

import static javax.persistence.FetchType.*;

@Entity
@Getter
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

    @Builder
    private Post(Category category, String subject, String filePath, String s3Key, PostStatus status) {
        this.category = category;
        this.subject = subject;
        this.filePath = filePath;
        this.s3Key = s3Key;
        this.uploadDate = LocalDate.now();
        this.status = status;
    }
}
