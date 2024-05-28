package potatowoong.potatomallback.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.common.ApiResponseEntity;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLoginLogController {

    private final AdminLoginLogService adminLoginLogService;

    /**
     * 관리자 로그인 내역 목록 조회 API
     */
    @GetMapping("/login-logs")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<AdminLoginLogResDto>>> findAdminLoginLogWithPage(PageRequestDto pageRequestDto) {
        PageResponseDto<AdminLoginLogResDto> result = adminLoginLogService.getAdminLoginLogWithPage(pageRequestDto);
        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }
}
