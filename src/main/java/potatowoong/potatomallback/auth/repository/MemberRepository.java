package potatowoong.potatomallback.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.auth.entity.Member;
import potatowoong.potatomallback.oauth.enums.SocialType;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByUserIdAndSocialType(String userId, SocialType socialType);
}
