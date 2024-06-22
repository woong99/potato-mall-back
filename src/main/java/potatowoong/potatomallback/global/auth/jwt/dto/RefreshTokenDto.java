package potatowoong.potatomallback.global.auth.jwt.dto;

import lombok.Builder;

@Builder
public record RefreshTokenDto(
    String token,
    long expiresIn
) {

    /**
     * 만료까지 남은 시간을 초 단위로 반환
     */
    public int getExpiresInSecond() {
        return (int) ((expiresIn - System.currentTimeMillis()) / 1000);
    }
}
