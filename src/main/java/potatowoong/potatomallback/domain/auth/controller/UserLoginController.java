package potatowoong.potatomallback.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.auth.service.TokenRefreshService;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.common.ApiResponseEntity;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserLoginController {

    private final TokenRefreshService tokenRefreshService;

    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseEntity<AccessTokenDto>> refresh(HttpServletRequest request) {
        // Access Token 갱신
        AccessTokenDto accessTokenDto = tokenRefreshService.userRefresh(request);

        return ResponseEntity.ok(ApiResponseEntity.of(accessTokenDto));
    }
}
