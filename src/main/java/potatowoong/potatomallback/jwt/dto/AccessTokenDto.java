package potatowoong.potatomallback.jwt.dto;

import lombok.Builder;

@Builder
public record AccessTokenDto(
    String token
) {

}
