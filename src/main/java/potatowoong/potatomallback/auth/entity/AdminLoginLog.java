package potatowoong.potatomallback.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import potatowoong.potatomallback.auth.enums.TryResult;

@Entity
@Comment("관리자 로그인 내역 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("관리자 로그인 내역 정보 IDX")
    private Long adminLoginLogId;

    @Column(nullable = false, length = 20, updatable = false)
    @Comment("관리자 ID")
    private String adminId;

    @Column(nullable = false, length = 30, updatable = false)
    @Comment("로그인 시도 IP")
    private String tryIp;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    @Comment("로그인 시도 결과(SUCCESS: 성공, FAIL: 실패, LOGOUT: 로그아웃)")
    private TryResult tryResult;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("로그인 시도 일시")
    private LocalDateTime tryDate;

    @Builder
    public AdminLoginLog(String adminId, String tryIp, TryResult tryResult) {
        this.adminId = adminId;
        this.tryIp = tryIp;
        this.tryResult = tryResult;
    }
}
