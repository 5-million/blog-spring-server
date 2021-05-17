package eol_g.blog.config.auth.dto;

import lombok.Builder;
import lombok.Getter;
import eol_g.blog.domain.user.Role;
import eol_g.blog.domain.user.User;

import java.util.Map;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private int id;
    private String nodeId;
    private String login;
    private String name;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes,
                           String nameAttributeKey,
                           int id, String nodeId, String login, String name) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.id = id;
        this.nodeId = nodeId;
        this.login = login;
        this.name = name;
    }

    /**
     * OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에 값 하나하나를 변환
     */
    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {
        return ofGithub(userNameAttributeName, attributes);
    }

    public User toEntity(int ADMIN_ID, String ADMIN_NODE_ID) {
        Role role = Role.GUEST;
        if (id == ADMIN_ID && nodeId.equals(ADMIN_NODE_ID))
            role = Role.ADMIN;

        return User.builder()
                .id(id)
                .nodeId(nodeId)
                .login(login)
                .name(name)
                .role(role)
                .build();
    }

    private static OAuthAttributes ofGithub(String userNameAttributeName,
                                            Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .id((Integer) attributes.get("id"))
                .nodeId((String) attributes.get("node_id"))
                .login((String) attributes.get("login"))
                .name((String) attributes.get("name"))
                .build();
    }
}
