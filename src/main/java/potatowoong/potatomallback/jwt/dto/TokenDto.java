package potatowoong.potatomallback.jwt.dto;

import lombok.Builder;

@Builder
public record TokenDto(
    String accessToken,
    String refreshToken
) {

}
