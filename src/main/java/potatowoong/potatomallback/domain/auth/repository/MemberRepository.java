package potatowoong.potatomallback.domain.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.global.auth.oauth.enums.SocialType;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByUserIdAndSocialType(String userId, SocialType socialType);

    boolean existsByUserId(String userId);

    boolean existsByNickname(String nickname);
}
