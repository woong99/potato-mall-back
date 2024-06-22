package potatowoong.potatomallback.domain.auth.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import potatowoong.potatomallback.domain.auth.enums.TryResult;

@Builder
public record AdminLoginLogResDto(Long adminLoginLogId, String adminId, String tryIp, TryResult tryResult, LocalDateTime tryDate) {

}
