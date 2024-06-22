package potatowoong.potatomallback.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.domain.auth.service.AdminLoginLogService;
import potatowoong.potatomallback.domain.auth.service.AdminLoginService;
import potatowoong.potatomallback.domain.auth.service.TokenRefreshService;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.utils.SecurityUtils;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminLoginService adminLoginService;

    private final AdminLoginLogService adminLoginLogService;

    private final TokenRefreshService tokenRefreshService;

    /**
     * 관리자 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseEntity<AccessTokenDto>> login(@Valid @RequestBody LoginReqDto loginReqDto, HttpServletResponse response) {
        // 로그인
        AccessTokenDto accessTokenDto = adminLoginService.login(loginReqDto, response);

        // 로그인 성공 시 로그 저장
        adminLoginLogService.addSuccessAdminLoginLog(loginReqDto.id());

        return ResponseEntity.ok(ApiResponseEntity.of(accessTokenDto));
    }

    /**
     * 관리자 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseEntity<String>> logout() {
        adminLoginLogService.addLogoutAdminLoginLog(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponseEntity.of("로그아웃 성공"));
    }

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseEntity<AccessTokenDto>> refresh(HttpServletRequest request) {
        // Access Token 갱신
        AccessTokenDto accessTokenDto = tokenRefreshService.adminRefresh(request);

        return ResponseEntity.ok(ApiResponseEntity.of(accessTokenDto));
    }
}
