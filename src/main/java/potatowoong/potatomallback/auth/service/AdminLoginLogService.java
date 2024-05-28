package potatowoong.potatomallback.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.auth.entity.AdminLoginLog;
import potatowoong.potatomallback.auth.enums.TryResult;
import potatowoong.potatomallback.auth.repository.AdminLoginLogRepository;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.utils.ClientUtils;

@Service
@RequiredArgsConstructor
public class AdminLoginLogService {

    private final AdminLoginLogRepository adminLoginLogRepository;

    @Transactional
    public void addSuccessAdminLoginLog(final String adminId) {
        AdminLoginLog adminLoginLog = AdminLoginLog.builder()
            .adminId(adminId)
            .tryIp(getTryIp())
            .tryResult(TryResult.SUCCESS)
            .build();
        adminLoginLogRepository.save(adminLoginLog);
    }

    @Transactional
    public void addFailAdminLoginLog(final String adminId) {
        AdminLoginLog adminLoginLog = AdminLoginLog.builder()
            .adminId(adminId)
            .tryIp(getTryIp())
            .tryResult(TryResult.FAIL)
            .build();
        adminLoginLogRepository.save(adminLoginLog);
    }

    @Transactional
    public void addLogoutAdminLoginLog(final String adminId) {
        AdminLoginLog adminLoginLog = AdminLoginLog.builder()
            .adminId(adminId)
            .tryIp(getTryIp())
            .tryResult(TryResult.LOGOUT)
            .build();
        adminLoginLogRepository.save(adminLoginLog);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AdminLoginLogResDto> getAdminLoginLogWithPage(PageRequestDto pageRequestDto) {
        return adminLoginLogRepository.findAdminLoginLogWithPage(pageRequestDto);
    }

    /**
     * 로그인 시도 IP 조회
     */
    private String getTryIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return ClientUtils.getRemoteIP(request);
    }
}
