package potatowoong.potatomallback.domain.pay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto;

@Entity
@Comment("결제 에러 로그 정보")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayErrorLog {

    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    @Comment("주문 ID")
    private String orderId;

    @Column(nullable = false, updatable = false, length = 100)
    @Comment("에러 코드")
    private String code;

    @Column(nullable = false, updatable = false, length = 1000)
    @Comment("에러 메시지")
    private String message;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("에러 발생 일자")
    private LocalDateTime errorDate;

    @Builder
    public PayErrorLog(String orderId, String code, String message) {
        this.orderId = orderId;
        this.code = code;
        this.message = message;
    }

    public static PayErrorLog of(UserPayReqDto.Error dto) {
        return PayErrorLog.builder()
            .code(dto.errorCode())
            .message(dto.errorMessage())
            .build();
    }
}
