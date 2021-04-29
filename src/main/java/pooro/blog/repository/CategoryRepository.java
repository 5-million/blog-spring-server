package pooro.blog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pooro.blog.domain.Category;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private final EntityManager em;

    public Category save(Category category) {
        em.persist(category);
        return category;
    }

    public Optional<Category> findOne(Long id) {
        Category category = em.find(Category.class, id);
        return Optional.ofNullable(category);
    }

    public Optional<Category> findByName(String name) {
        List<Category> result =  em.createQuery("select c from Category c where c.name = :name")
                .setParameter("name", name)
                .getResultList();

        return result.stream().findAny();
    }

    public Optional<List<Category>> findAll() {
        List<Category> categories = em.createQuery("select c from Category c").getResultList();
        return Optional.ofNullable(categories);
    }
}
