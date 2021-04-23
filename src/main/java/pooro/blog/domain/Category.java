package pooro.blog.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Post> post = new ArrayList<>();

    public static Category createCategory(String name) {
        Category category = new Category();
        category.name = name;

        return category;
    }

    public static Category createCategory(Long id, String name) {
        Category category = new Category();
        category.id = id;
        category.name = name;

        return category;
    }
}
