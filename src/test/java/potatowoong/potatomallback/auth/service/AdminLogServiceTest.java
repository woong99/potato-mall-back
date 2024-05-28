package potatowoong.potatomallback.auth.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import potatowoong.potatomallback.auth.repository.AdminLogRepository;

@ExtendWith(MockitoExtension.class)
class AdminLogServiceTest {

    @Mock
    private AdminLogRepository adminLogRepository;

    @InjectMocks
    private AdminLogService adminLogService;

    @Test
    @DisplayName("관리자 활동 내역 저장 성공 - 모든 파라미터 있는 경우")
    void 관리자_활동_내역_저장_성공_all() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        adminLogService.addAdminLog("menuTitle", "action", 1, "targetName");

        // then
        then(adminLogRepository).should().save(any());
    }

    @Test
    @DisplayName("관리자 활동 내역 저장 성공 - 대상 ID, 이름 없는 경우")
    void 관리자_활동_내역_저장_성공() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        adminLogService.addAdminLog("menuTitle", "action");

        // then
        then(adminLogRepository).should().save(any());
    }
}