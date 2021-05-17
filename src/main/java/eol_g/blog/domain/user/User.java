package eol_g.blog.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private int id;

    private String nodeId;
    private String login;
    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(int id, String nodeId, String login, String name, Role role) {
        this.id = id;
        this.nodeId = nodeId;
        this.login = login;
        this.name = name;
        this.role = role;
    }

    public String getRoleKey() {
        return role.getKey();
    }
}
