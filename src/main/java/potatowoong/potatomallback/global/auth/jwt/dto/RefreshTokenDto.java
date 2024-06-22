package potatowoong.potatomallback.global.auth.jwt.dto;

import lombok.Builder;

@Builder
public record RefreshTokenDto(
    String token,
    long expiresIn
) {

}
