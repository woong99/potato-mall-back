package potatowoong.potatomallback.auth.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import potatowoong.potatomallback.auth.entity.Admin;

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
