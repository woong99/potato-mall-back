package potatowoong.potatomallback.auth.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import potatowoong.potatomallback.auth.enums.TryResult;

@Builder
public record AdminLoginLogResDto(Long adminLoginLogId, String adminId, String tryIp, TryResult tryResult, LocalDateTime tryDate) {

}
