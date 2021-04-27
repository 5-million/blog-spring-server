package pooro.blog.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Admin {
    @Value("${admin.github.id}")
    private int ADMIN_ID;

    @Value("${admin.github.node-id}")
    private String ADMIN_NODE_ID;
}
