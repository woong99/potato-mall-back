package potatowoong.potatomallback.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.auth.enums.TryResult;
import potatowoong.potatomallback.auth.repository.AdminLoginLogRepository;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

@ExtendWith(MockitoExtension.class)
class AdminLoginLogServiceTest {

    private final String adminId = "adminId";

    @Mock
    private AdminLoginLogRepository adminLoginLogRepository;

    @InjectMocks
    private AdminLoginLogService adminLoginLogService;

    @Test
    @DisplayName("관리자 로그인 내역 저장 성공 - 로그인 성공")
    void 관리자_로그인_내역_저장_성공_로그인_성공() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        adminLoginLogService.addSuccessAdminLoginLog(adminId);

        // then
        then(adminLoginLogRepository).should().save(any());
    }

    @Test
    @DisplayName("관리자 로그인 내역 저장 성공 - 로그인 실패")
    void 관리자_로그인_내역_저장_성공_로그인_실패() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        adminLoginLogService.addFailAdminLoginLog(adminId);

        // then
        then(adminLoginLogRepository).should().save(any());
    }

    @Test
    @DisplayName("관리자 로그인 내역 저장 성공 - 로그아웃")
    void 관리자_로그인_내역_저장_성공_로그아웃() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        adminLoginLogService.addSuccessAdminLoginLog(adminId);

        // then
        then(adminLoginLogRepository).should().save(any());
    }

    @Test
    @DisplayName("관리자 로그인 내역 목록 조회 성공")
    void 관리자_로그인_내역_목록_조회_성공() {
        // given
        PageRequestDto pageRequestDto = new PageRequestDto("", "", 0, 10, "", SortDirection.DESCENDING);
        PageResponseDto<AdminLoginLogResDto> expectedResponse = new PageResponseDto<>(List.of(getAdminLoginLogResDto()), 10);

        given(adminLoginLogRepository.findAdminLoginLogWithPage(pageRequestDto)).willReturn(expectedResponse);

        // when
        PageResponseDto<AdminLoginLogResDto> actualResponse = adminLoginLogService.getAdminLoginLogWithPage(pageRequestDto);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        then(adminLoginLogRepository).should().findAdminLoginLogWithPage(pageRequestDto);
    }

    private AdminLoginLogResDto getAdminLoginLogResDto() {
        return AdminLoginLogResDto.builder()
            .adminLoginLogId(1L)
            .adminId("adminId")
            .tryIp("tryIp")
            .tryResult(TryResult.SUCCESS)
            .tryDate(LocalDateTime.now())
            .build();
    }
}