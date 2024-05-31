package potatowoong.potatomallback.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Comment;
import org.springframework.data.domain.Persistable;
import potatowoong.potatomallback.auth.dto.request.AdminAddReqDto;
import potatowoong.potatomallback.config.db.BaseEntity;

@Entity
@Comment("관리자 계정 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity implements Persistable<String> {

    @Id
    @Column(name = "user_id", length = 20, nullable = false, updatable = false)
    @Comment("아이디")
    private String adminId;

    @Column(name = "password", length = 100, nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(name = "name", length = 10, nullable = false)
    @Comment("이름")
    private String name;

    @Builder
    public Admin(String adminId, String password, String name) {
        this.adminId = adminId;
        this.password = password;
        this.name = name;
    }

    public static Admin addOf(AdminAddReqDto dto) {
        return Admin.builder()
            .adminId(dto.getAdminId())
            .password(dto.getPassword())
            .name(dto.getName())
            .build();
    }

    public void modify(final String name, final String password) {
        this.name = name;
        if (StringUtils.isNotBlank(password)) {
            this.password = password;
        }
    }

    @Override
    public String getId() {
        return this.adminId;
    }

    @Override
    public boolean isNew() {
        return super.getCreatedAt() == null;
    }
}
