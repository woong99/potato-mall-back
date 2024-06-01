package potatowoong.potatomallback.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import potatowoong.potatomallback.auth.entity.AdminLog;
import potatowoong.potatomallback.auth.repository.AdminLogRepository;
import potatowoong.potatomallback.utils.ClientUtils;
import potatowoong.potatomallback.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    public void addAdminLog(final String menuTitle, final String action, final int targetId, final String targetName) {
        AdminLog adminLog = AdminLog.builder()
            .adminId(SecurityUtils.getCurrentUserId())
            .menuTitle(menuTitle)
            .action(action)
            .targetId(String.valueOf(targetId))
            .targetName(targetName)
            .actionIp(ClientUtils.getRemoteIP())
            .build();
        adminLogRepository.save(adminLog);
    }

    public void addAdminLog(final String menuTitle, final String action, final long targetId, final String targetName) {
        AdminLog adminLog = AdminLog.builder()
            .adminId(SecurityUtils.getCurrentUserId())
            .menuTitle(menuTitle)
            .action(action)
            .targetId(String.valueOf(targetId))
            .targetName(targetName)
            .actionIp(ClientUtils.getRemoteIP())
            .build();
        adminLogRepository.save(adminLog);
    }

    public void addAdminLog(final String menuTitle, final String action, final String targetId, final String targetName) {
        AdminLog adminLog = AdminLog.builder()
            .adminId(SecurityUtils.getCurrentUserId())
            .menuTitle(menuTitle)
            .action(action)
            .targetId(String.valueOf(targetId))
            .targetName(targetName)
            .actionIp(ClientUtils.getRemoteIP())
            .build();
        adminLogRepository.save(adminLog);
    }

    public void addAdminLog(final String menuTitle, final String action, final String targetId) {
        AdminLog adminLog = AdminLog.builder()
            .adminId(SecurityUtils.getCurrentUserId())
            .menuTitle(menuTitle)
            .action(action)
            .targetId(String.valueOf(targetId))
            .actionIp(ClientUtils.getRemoteIP())
            .build();
        adminLogRepository.save(adminLog);
    }

    public void addAdminLog(final String menuTitle, final String action) {
        AdminLog adminLog = AdminLog.builder()
            .adminId(SecurityUtils.getCurrentUserId())
            .menuTitle(menuTitle)
            .action(action)
            .actionIp(ClientUtils.getRemoteIP())
            .build();
        adminLogRepository.save(adminLog);
    }
}
