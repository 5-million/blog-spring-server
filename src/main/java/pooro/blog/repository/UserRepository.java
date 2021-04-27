package pooro.blog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pooro.blog.domain.user.User;

import javax.persistence.EntityManager;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public Optional<User> save(User user) {
        em.persist(user);
        return Optional.ofNullable(user);
    }

    public Optional<User> findById(int id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }
}
