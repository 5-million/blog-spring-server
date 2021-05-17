package eol_g.blog.repository;

import eol_g.blog.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
