package potatowoong.potatomallback.auth.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import potatowoong.potatomallback.auth.entity.Admin;
import potatowoong.potatomallback.config.db.UseFlag;

@Builder
public record AdminResDto(
    String adminId,
    String name,
    LocalDateTime updatedAt,
    UseFlag useFlag
) {

    public static AdminResDto of(Admin admin) {
        return new AdminResDto(
            admin.getAdminId(),
            admin.getName(),
            admin.getUpdatedAt(),
            admin.getUseFlag()
        );
    }
}
