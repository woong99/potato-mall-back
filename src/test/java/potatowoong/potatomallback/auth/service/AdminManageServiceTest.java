package potatowoong.potatomallback.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import potatowoong.potatomallback.auth.dto.request.AdminAddReqDto;
import potatowoong.potatomallback.auth.dto.request.AdminModifyReqDto;
import potatowoong.potatomallback.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.auth.entity.Admin;
import potatowoong.potatomallback.auth.repository.AdminRepository;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class AdminManageServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminManageService adminManageService;

    private final String adminId = "adminId";

    private final String name = "name";

    private final String password = "password";

    @Nested
    @DisplayName("관리자 계정 등록")
    class 관리자_계정_등록 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            AdminAddReqDto dto = AdminAddReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.empty());

            // when
            adminManageService.addAdmin(dto);

            // then
            then(adminRepository).should().save(any());
            then(passwordEncoder).should().encode(password);
        }

        @Test
        @DisplayName("실패 - 아이디 중복")
        void 실패_아이디_중복() {
            // given
            AdminAddReqDto dto = AdminAddReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.of(Admin.builder().build()));

            // when
            assertThatThrownBy(() -> adminManageService.addAdmin(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXIST_USER_ID);

            // then
            then(adminRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void 실패_비밀번호_불일치() {
            // given
            AdminAddReqDto dto = AdminAddReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm("invalidPassword")
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> adminManageService.addAdmin(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM);

            // then
            then(adminRepository).should().findById(any());
        }
    }

    @Nested
    @DisplayName("관리자 ID 중복 체크")
    class 관리자_ID_중복_체크 {

        @Test
        @DisplayName("성공 - 중복 X")
        void 성공_중복X() {
            // given
            given(adminRepository.existsById(any())).willReturn(false);

            // when
            boolean result = adminManageService.checkDuplicateAdminId(adminId);

            // then
            assertThat(result).isFalse();
            then(adminRepository).should().existsById(adminId);
        }

        @Test
        @DisplayName("성공 - 중복 O")
        void 성공_중복O() {
            // given
            given(adminRepository.existsById(any())).willReturn(true);

            // when
            boolean result = adminManageService.checkDuplicateAdminId(adminId);

            // then
            assertThat(result).isTrue();
            then(adminRepository).should().existsById(adminId);
        }
    }

    @Nested
    @DisplayName("관리자 계정 수정")
    class 관리자_계정_수정 {

        @Test
        @DisplayName("성공 - 비밀번호 변경 X")
        void 성공_비밀번호_변경X() {
            // given
            AdminModifyReqDto dto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.of(createAdmin()));

            // when
            adminManageService.modifyAdmin(dto);

            // then
            then(adminRepository).should().findById(any());
            then(adminRepository).should().save(any());
        }

        @Test
        @DisplayName("성공 - 비밀번호 변경 O")
        void 성공_비밀번호_변경O() {
            // given
            AdminModifyReqDto dto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm(password)
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.of(createAdmin()));

            // when
            adminManageService.modifyAdmin(dto);

            // then
            then(adminRepository).should().findById(any());
            then(adminRepository).should().save(any());
            then(passwordEncoder).should().encode(password);
        }

        @Test
        @DisplayName("실패 - 잘못된 ID")
        void 실패_잘못된_ID() {
            // given
            AdminModifyReqDto dto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> adminManageService.modifyAdmin(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_NOT_FOUND);

            // then
            then(adminRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void 실패_비밀번호_불일치() {
            // given
            AdminModifyReqDto dto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password(password)
                .passwordConfirm("invalidPassword")
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.of(createAdmin()));

            // when
            assertThatThrownBy(() -> adminManageService.modifyAdmin(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM);

            // then
            then(adminRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패 - 비밀번호 길이 부족")
        void 실패_비밀번호_길이_부족() {
            // given
            AdminModifyReqDto dto = AdminModifyReqDto.builder()
                .adminId(adminId)
                .name(name)
                .password("1234")
                .passwordConfirm("1234")
                .build();
            given(adminRepository.findById(any())).willReturn(Optional.of(createAdmin()));

            // when
            assertThatThrownBy(() -> adminManageService.modifyAdmin(dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INCORRECT_PASSWORD_LENGTH);

            // then
            then(adminRepository).should().findById(any());
        }
    }

    @Nested
    @DisplayName("관리자 계정 삭제")
    class 관리자_계정_삭제 {

        @Test
        @DisplayName("성공")
        void 삭제_성공() {
            // given
            given(adminRepository.findById(any())).willReturn(Optional.of(Admin.builder()
                .adminId(adminId)
                .build()));

            // when
            try (MockedStatic<SecurityUtils> util = mockStatic(SecurityUtils.class)) {
                util.when(SecurityUtils::getCurrentUserId).thenReturn("anotherId");
                adminManageService.removeAdmin(adminId);
            }

            // then
            then(adminRepository).should().findById(any());
            then(adminRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 자신의 ID")
        void 실패_자신의_ID() {
            // given
            given(adminRepository.findById(any())).willReturn(Optional.of(Admin.builder()
                .adminId(adminId)
                .build()));

            // when
            try (MockedStatic<SecurityUtils> util = mockStatic(SecurityUtils.class)) {
                util.when(SecurityUtils::getCurrentUserId).thenReturn(adminId);
                assertThatThrownBy(() -> adminManageService.removeAdmin(adminId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SELF_DELETION_NOT_ALLOWED);
            }

            // then
            then(adminRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패 - 잘못된 ID")
        void 실패_잘못된_ID() {
            // given
            given(adminRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> adminManageService.removeAdmin(adminId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_NOT_FOUND);

            // then
            then(adminRepository).should().findById(any());
        }
    }

    @Nested
    @DisplayName("관리자 계정 상세 조회")
    class 관리자_계정_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(adminRepository.findById(any())).willReturn(Optional.of(Admin.builder()
                .adminId(adminId)
                .build()));

            // when
            AdminResDto result = adminManageService.getAdmin(adminId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.adminId()).isEqualTo(adminId);
            then(adminRepository).should().findById(any());
        }

        @Test
        @DisplayName("실패")
        void 실패() {
            // given
            given(adminRepository.findById(any())).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> adminManageService.getAdmin(adminId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_NOT_FOUND);

            // then
            then(adminRepository).should().findById(any());
        }
    }

    @Nested
    @DisplayName("관리자 계정 목록 조회")
    class 관리자_계정_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(adminRepository.findAdminWithPage(any())).willReturn(new PageResponseDto<>(Collections.emptyList(), 100));

            // when
            PageResponseDto<AdminResDto> result = adminManageService.getAdminList(any());

            // then
            assertThat(result).isNotNull();
            assertThat(result.totalElements()).isEqualTo(100);
            then(adminRepository).should().findAdminWithPage(any());
        }
    }

    private Admin createAdmin() {
        return Admin.builder()
            .adminId(adminId)
            .name(name)
            .password(password)
            .build();
    }
}