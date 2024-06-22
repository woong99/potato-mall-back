package potatowoong.potatomallback.domain.auth.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import potatowoong.potatomallback.domain.auth.entity.Admin;

@Builder
public record AdminResDto(
    String adminId,
    String name,
    LocalDateTime updatedAt
) {

    public static AdminResDto of(Admin admin) {
        return new AdminResDto(
            admin.getAdminId(),
            admin.getName(),
            admin.getUpdatedAt()
        );
    }
}
