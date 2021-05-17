package eol_g.blog.config.auth.dto;

import lombok.Getter;
import lombok.ToString;
import eol_g.blog.domain.user.User;

import java.io.Serializable;

@Getter
@ToString
public class SessionUser implements Serializable {
    private int id;
    private String nodeId;
    private String login;
    private String name;

    public SessionUser(User user) {
        this.id = user.getId();
        this.nodeId = user.getNodeId();
        this.login = user.getLogin();
        this.name = user.getName();
    }
}
