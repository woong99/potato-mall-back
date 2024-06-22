package potatowoong.potatomallback.oauth.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.auth.entity.Member;
import potatowoong.potatomallback.auth.repository.MemberRepository;
import potatowoong.potatomallback.oauth.CustomOAuth2User;
import potatowoong.potatomallback.oauth.OAuth2UserInfoFactory;
import potatowoong.potatomallback.oauth.data.OAuth2UserInfo;
import potatowoong.potatomallback.oauth.enums.SocialType;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        final SocialType socialType = getSocialType(registrationId);

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.of(getSocialType(registrationId), attributes);

        // 이미 가입된 회원인지 확인 후 가입되지 않은 회원이면 저장
        Optional<Member> member = memberRepository.findByUserIdAndSocialType(oAuth2UserInfo.getId(), socialType);
        if (member.isEmpty()) {
            saveMember(oAuth2UserInfo, socialType);
        }

        return new CustomOAuth2User(
            socialType == SocialType.NAVER ? (Map<String, Object>) oAuth2User.getAttributes().get("response") : oAuth2User.getAttributes(),
            socialType == SocialType.NAVER ? "id" : userNameAttributeName,
            oAuth2UserInfo.getId()
        );
    }

    /**
     * SocialType 반환
     */
    private SocialType getSocialType(final String registrationId) {
        return SocialType.valueOf(registrationId.toUpperCase());
    }

    /**
     * 회원 저장
     */
    private void saveMember(OAuth2UserInfo oAuth2UserInfo, SocialType socialType) {
        Member member = Member.builder()
            .userId(oAuth2UserInfo.getId())
            .nickname("user" + UUID.randomUUID().toString().replace("-", "").substring(16))
            .socialType(socialType)
            .updatedBy(oAuth2UserInfo.getId())
            .build();
        memberRepository.save(member);
    }
}
