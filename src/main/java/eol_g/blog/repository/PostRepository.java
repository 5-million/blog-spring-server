package eol_g.blog.repository;

import eol_g.blog.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final EntityManager em;

    public Long save(Post post) {
        em.persist(post);
        return post.getId();
    }

    public Optional<Post> findOne(Long postId) {
        Post post = em.find(Post.class, postId);
        return Optional.ofNullable(post);
    }

    public Optional<Post> findBySubject(String subject) {
        List<Post> result = em.createQuery("select p from Post p where p.subject = :subject")
                .setParameter("subject", subject)
                .getResultList();

        return result.stream().findAny();
    }

    public Optional<List<Post>> findAll() {
        List<Post> result = em.createQuery("select p from Post p").getResultList();
        return Optional.ofNullable(result);
    }

    public Optional<List<Post>> findByCategory(String category) {
        List<Post> result = em.createQuery("select p from Post p where p.category.name = :category")
                .setParameter("category", category)
                .getResultList();

        return Optional.ofNullable(result);
    }
}
