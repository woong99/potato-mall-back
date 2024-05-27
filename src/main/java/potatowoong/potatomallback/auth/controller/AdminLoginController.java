package potatowoong.potatomallback.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.auth.dto.request.LoginReqDto;
import potatowoong.potatomallback.auth.service.AdminLoginService;
import potatowoong.potatomallback.common.ApiResponseEntity;
import potatowoong.potatomallback.jwt.dto.TokenDto;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminLoginService adminLoginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseEntity<TokenDto>> login(@Valid @RequestBody LoginReqDto loginReqDto) {
        TokenDto tokenDto = adminLoginService.login(loginReqDto);

        return ResponseEntity.ok(ApiResponseEntity.of(tokenDto));
    }
}
