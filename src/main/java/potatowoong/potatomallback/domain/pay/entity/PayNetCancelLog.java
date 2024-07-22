package potatowoong.potatomallback.domain.pay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Comment("망취소 로그 정보")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayNetCancelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("망취소 로그 정보 IDX")
    private Long payNetCancelLogId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @Comment("주문 ID")
    private TossPaymentPayTransaction tossPaymentPayTransaction;

    @Column(nullable = false, updatable = false, length = 200)
    @Comment("결제 키")
    private String paymentKey;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("에러 발생 일자")
    private LocalDateTime errorDate;

    @Builder
    public PayNetCancelLog(TossPaymentPayTransaction tossPaymentPayTransaction, String paymentKey) {
        this.tossPaymentPayTransaction = tossPaymentPayTransaction;
        this.paymentKey = paymentKey;
    }
}
