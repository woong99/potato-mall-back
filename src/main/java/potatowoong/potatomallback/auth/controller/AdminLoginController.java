package potatowoong.potatomallback.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.auth.service.AdminLoginService;
import potatowoong.potatomallback.common.ApiResponseEntity;
import potatowoong.potatomallback.jwt.dto.TokenDto;
import potatowoong.potatomallback.utils.SecurityUtils;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminLoginService adminLoginService;

    private final AdminLoginLogService adminLoginLogService;

    /**
     * 관리자 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseEntity<TokenDto>> login(@Valid @RequestBody LoginReqDto loginReqDto) {
        // 로그인
        TokenDto tokenDto = adminLoginService.login(loginReqDto);

        // 로그인 성공 시 로그 저장
        adminLoginLogService.addSuccessAdminLoginLog(loginReqDto.id());

        return ResponseEntity.ok(ApiResponseEntity.of(tokenDto));
    }

    /**
     * 관리자 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseEntity<String>> logout() {
        adminLoginLogService.addLogoutAdminLoginLog(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponseEntity.of("로그아웃 성공"));
    }
}
