package potatowoong.potatomallback.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.auth.dto.request.UserSignUpReqDto;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class UserLoginService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(UserSignUpReqDto dto) {
        // 아이디 중복 체크
        if (memberRepository.existsByUserId(dto.getUserId())) {
            throw new CustomException(ErrorCode.DUPLICATE_USER_ID);
        }

        // 닉네임 중복 체크
        if (memberRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 비밀번호 일치 여부 체크
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        // 비밀번호 암호화
        dto.modifyPassword(passwordEncoder.encode(dto.getPassword()));

        Member member = Member.of(dto);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public boolean checkDuplicateId(final String userId) {
        return memberRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean checkDuplicateNickname(final String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
