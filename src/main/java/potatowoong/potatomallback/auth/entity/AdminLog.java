package potatowoong.potatomallback.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Comment("관리자 활동 내역 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("관리자 활동 내역 정보 IDX")
    private Long adminLogId;

    @Column(nullable = false, updatable = false, length = 20)
    @Comment("관리자 ID")
    private String adminId;

    @Column(nullable = false, updatable = false, length = 50)
    @Comment("메뉴명")
    private String menuTitle;

    @Column(nullable = false, updatable = false, length = 200)
    @Comment("행동")
    private String action;

    @Column(updatable = false, length = 100)
    @Comment("대상 ID")
    private String targetId;

    @Column(updatable = false, length = 200)
    @Comment("대상 이름")
    private String targetName;

    @Column(nullable = false, updatable = false, length = 30)
    @Comment("활동 IP")
    private String actionIp;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("활동 일시")
    private LocalDateTime actionDate;

    @Builder
    public AdminLog(String adminId, String menuTitle, String action, String targetId, String targetName, String actionIp) {
        this.adminId = adminId;
        this.menuTitle = menuTitle;
        this.action = action;
        this.targetId = targetId;
        this.targetName = targetName;
        this.actionIp = actionIp;
    }
}
