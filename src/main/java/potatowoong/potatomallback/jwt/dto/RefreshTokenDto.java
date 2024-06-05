package potatowoong.potatomallback.jwt.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RefreshTokenDto(
    String token,
    LocalDateTime tokenExpiresIn
) {

}
