package potatowoong.potatomallback.jwt.dto;

import lombok.Builder;

@Builder
public record TokenDto(
    AccessTokenDto accessTokenDto,
    RefreshTokenDto refreshTokenDto
) {

}