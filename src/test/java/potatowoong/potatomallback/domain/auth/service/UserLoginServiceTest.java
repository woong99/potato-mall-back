package potatowoong.potatomallback.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import potatowoong.potatomallback.domain.auth.dto.request.UserSignUpReqDto;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserLoginService userLoginService;

    @Nested
    @DisplayName("회원가입")
    class 회원가입 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            given(memberRepository.existsByUserId(any())).willReturn(false);
            given(memberRepository.existsByNickname(any())).willReturn(false);

            // when
            userLoginService.signUp(dto);

            // then
            then(memberRepository).should().existsByUserId(any());
            then(memberRepository).should().existsByNickname(any());
            then(memberRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 아이디 중복")
        void 실패_아이디_중복() {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            given(memberRepository.existsByUserId(any())).willReturn(true);

            // when
            assertThatThrownBy(() -> userLoginService.signUp(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USER_ID);

            // then
            then(memberRepository).should().existsByUserId(any());
            then(memberRepository).should(never()).existsByNickname(any());
            then(memberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 닉네임 중복")
        void 실패_닉네임_중복() {
            // given
            UserSignUpReqDto dto = getUserSignUpReqDto();

            given(memberRepository.existsByUserId(any())).willReturn(false);
            given(memberRepository.existsByNickname(any())).willReturn(true);

            // when
            assertThatThrownBy(() -> userLoginService.signUp(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

            // then
            then(memberRepository).should().existsByUserId(any());
            then(memberRepository).should().existsByNickname(any());
            then(memberRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void 실패_비밀번호_불일치() {
            // given
            UserSignUpReqDto dto = UserSignUpReqDto.builder()
                .userId("userId")
                .password("password")
                .passwordConfirm("incorrectPassword")
                .nickname("nickname")
                .build();

            given(memberRepository.existsByUserId(any())).willReturn(false);
            given(memberRepository.existsByNickname(any())).willReturn(false);

            // when
            assertThatThrownBy(() -> userLoginService.signUp(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NOT_MATCHED);

            // then
            then(memberRepository).should().existsByUserId(any());
            then(memberRepository).should().existsByNickname(any());
            then(memberRepository).should(never()).save(any());
        }

        private UserSignUpReqDto getUserSignUpReqDto() {
            return UserSignUpReqDto.builder()
                .userId("userId")
                .password("password")
                .passwordConfirm("password")
                .nickname("nickname")
                .build();
        }
    }

    @Nested
    @DisplayName("아이디 중복 검사")
    class 아이디_중복_검사 {

        @Test
        @DisplayName("중복된 아이디가 존재하는 경우")
        void 중복된_아이디가_존재하는_경우() {
            // given
            given(memberRepository.existsByUserId(any())).willReturn(true);

            // when
            boolean result = userLoginService.checkDuplicateId(any());

            // then
            assertThat(result).isTrue();

            then(memberRepository).should().existsByUserId(any());
        }

        @Test
        @DisplayName("중복된 아이디가 존재하지 않는 경우")
        void 중복된_아이디가_존재하지_않는_경우() {
            // given
            given(memberRepository.existsByUserId(any())).willReturn(false);

            // when
            boolean result = userLoginService.checkDuplicateId(any());

            // then
            assertThat(result).isFalse();

            then(memberRepository).should().existsByUserId(any());
        }
    }

    @Nested
    @DisplayName("닉네임 중복 검사")
    class 닉네임_중복_검사 {

        @Test
        @DisplayName("중복된 닉네임이 존재하는 경우")
        void 중복된_닉네임이_존재하는_경우() {
            // given
            given(memberRepository.existsByNickname(any())).willReturn(true);

            // when
            boolean result = userLoginService.checkDuplicateNickname(any());

            // then
            assertThat(result).isTrue();

            then(memberRepository).should().existsByNickname(any());
        }

        @Test
        @DisplayName("중복된 닉네임이 존재하지 않는 경우")
        void 중복된_닉네임이_존재하지_않는_경우() {
            // given
            given(memberRepository.existsByNickname(any())).willReturn(false);

            // when
            boolean result = userLoginService.checkDuplicateNickname(any());

            // then
            assertThat(result).isFalse();

            then(memberRepository).should().existsByNickname(any());
        }
    }
}