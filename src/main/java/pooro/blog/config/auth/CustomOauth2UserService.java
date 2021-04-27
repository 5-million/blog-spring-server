package pooro.blog.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pooro.blog.config.auth.dto.OAuthAttributes;
import pooro.blog.config.auth.dto.SessionUser;
import pooro.blog.domain.Admin;
import pooro.blog.domain.user.User;
import pooro.blog.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private final Admin admin;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // OAuth 서비스를 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        //OAuth2 로그인 진행 시 키가 되는 필드값
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes());

        User user = saveUser(attributes);

        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(
                        new SimpleGrantedAuthority(user.getRoleKey()) // 권한 주는 함수
                ), attributes.getAttributes(), attributes.getNameAttributeKey()
        );
    }

    /**
     * 처음 접속하는 사용자는 저장
     */
    private User saveUser(OAuthAttributes attributes) {
        User user = attributes.toEntity(admin.getADMIN_ID(), admin.getADMIN_NODE_ID());
        Optional<User> optUser = userRepository.findById(user.getId());

        if (optUser.isPresent()) return optUser.get(); // 기존에 있는 사용자면 저장하지 않음
        else return userRepository.save(user).get(); // 처음 접속하는 사용자면 저장
    }
}
