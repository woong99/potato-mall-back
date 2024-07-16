package potatowoong.potatomallback.domain.pay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Comment("결제 정보 - 결제 트랜잭션 정보 연결 테이블")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TossPaymentPayTransaction implements Persistable<String> {

    @Id
    @Column(nullable = false, updatable = false, length = 36, columnDefinition = "CHAR(36)")
    @Comment("주문 ID")
    private String orderId;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("트랜잭션 생성일자")
    private LocalDateTime transactionDate;

    @OneToMany(mappedBy = "tossPaymentPayTransaction")
    @ToString.Exclude
    private List<PayTransaction> payTransactions;

    @Builder
    public TossPaymentPayTransaction(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String getId() {
        return this.orderId;
    }

    @Override
    public boolean isNew() {
        return this.transactionDate == null;
    }
}
