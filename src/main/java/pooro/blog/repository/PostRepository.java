package pooro.blog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pooro.blog.domain.Post;

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
}