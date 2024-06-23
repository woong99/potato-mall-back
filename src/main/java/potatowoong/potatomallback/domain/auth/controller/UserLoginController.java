package potatowoong.potatomallback.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.auth.dto.request.UserSignUpReqDto;
import potatowoong.potatomallback.domain.auth.service.TokenRefreshService;
import potatowoong.potatomallback.domain.auth.service.UserLoginService;
import potatowoong.potatomallback.global.auth.jwt.dto.AccessTokenDto;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.ResponseText;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    private final TokenRefreshService tokenRefreshService;

    /**
     * 회원가입 API
     */
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponseEntity<String>> signUp(@Valid @RequestBody UserSignUpReqDto dto) {
        // 회원가입
        userLoginService.signUp(dto);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_SIGN_UP));
    }

    /**
     * 아이디 중복 체크 API
     */
    @GetMapping("/check-duplicate-id")
    public ResponseEntity<ApiResponseEntity<String>> checkDuplicateId(@RequestParam String userId) {
        // 아이디 중복 체크
        boolean isDuplicate = userLoginService.checkDuplicateId(userId);

        return ResponseEntity.ok(ApiResponseEntity.of(isDuplicate ? ResponseText.DUPLICATE : ResponseText.OK));
    }

    /**
     * 닉네임 중복 체크 API
     */
    @GetMapping("/check-duplicate-nickname")
    public ResponseEntity<ApiResponseEntity<String>> checkDuplicateNickname(@RequestParam String nickname) {
        // 닉네임 중복 체크
        boolean isDuplicate = userLoginService.checkDuplicateNickname(nickname);

        return ResponseEntity.ok(ApiResponseEntity.of(isDuplicate ? ResponseText.DUPLICATE : ResponseText.OK));
    }

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
