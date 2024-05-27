package potatowoong.potatomallback.config.db;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BaseEntity {

    @CreatedDate
    @Column(name = "reg_date", updatable = false, nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("등록일자")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "reg_id", updatable = false, nullable = false)
    @Comment("등록자")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "upd_date", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("수정일자")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "upd_id", nullable = false)
    @Comment("수정자")
    private String updatedBy;

    @Column(name = "use_flag", nullable = false, insertable = false)
    @Enumerated(EnumType.STRING)
    @Comment("사용여부")
    @ColumnDefault("'Y'")
    private UseFlag useFlag;

    public void deleteEntity() {
        this.useFlag = UseFlag.N;
    }
}
