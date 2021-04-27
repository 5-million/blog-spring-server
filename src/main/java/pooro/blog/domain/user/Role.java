package pooro.blog.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("ROLE_ADMIN", "관리자"),
    GUEST("ROLE_GUEST", "사용자");

    private final String key;
    private final String type;
}
