package potatowoong.potatomallback.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginReqDto(
    @NotBlank(message = "아이디를 입력해주세요.") String id,
    @NotBlank(message = "비밀번호를 입력해주세요.") String password) {

}
