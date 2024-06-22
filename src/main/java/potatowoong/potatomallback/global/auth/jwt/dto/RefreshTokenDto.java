package potatowoong.potatomallback.global.auth.jwt.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RefreshTokenDto(
    String token,
    LocalDateTime tokenExpiresIn
) {

}
