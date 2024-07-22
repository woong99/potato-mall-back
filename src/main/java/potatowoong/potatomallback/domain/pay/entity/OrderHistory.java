package potatowoong.potatomallback.domain.pay.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.pay.enums.PurchaseHistoryStatus;

@Entity
@Comment("구매 내역 정보")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @Comment("사용자 ID")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @Comment("주문 ID")
    private TossPayment tossPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("상태(SUCCESS: 결제 완료, CANCEL: 결제 취소, PREPARE_PRODUCT: 상품준비중, PREPARE_DELIVERY: 배송준비중, SHIPPING: 배송중, COMPLETE: 배송완료)")
    private PurchaseHistoryStatus status;

    @Column(nullable = false, updatable = false)
    @Comment("총 결제 금액")
    private int totalAmount;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("결제완료 일자")
    private LocalDateTime successDate;

    @Column(updatable = false, columnDefinition = "DATETIME")
    @Comment("결제취소 일자")
    private LocalDateTime cancelDate;

    @Column(updatable = false, columnDefinition = "DATETIME")
    @Comment("상품준비중 일자")
    private LocalDateTime prepareProductDate;

    @Column(updatable = false, columnDefinition = "DATETIME")
    @Comment("배송준비중 일자")
    private LocalDateTime prepareDeliveryDate;

    @Column(updatable = false, columnDefinition = "DATETIME")
    @Comment("배송중 일자")
    private LocalDateTime shippingDate;

    @Column(updatable = false, columnDefinition = "DATETIME")
    @Comment("배송완료 일자")
    private LocalDateTime completeDate;

    @OneToMany(mappedBy = "orderHistory", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderProduct> orderProducts;

    @Builder
    public OrderHistory(Member member, TossPayment tossPayment, PurchaseHistoryStatus status, int totalAmount, LocalDateTime successDate, LocalDateTime cancelDate, LocalDateTime prepareProductDate, LocalDateTime prepareDeliveryDate, LocalDateTime shippingDate, LocalDateTime completeDate,
        List<OrderProduct> orderProducts) {
        this.member = member;
        this.tossPayment = tossPayment;
        this.status = status;
        this.totalAmount = totalAmount;
        this.successDate = successDate;
        this.cancelDate = cancelDate;
        this.prepareProductDate = prepareProductDate;
        this.prepareDeliveryDate = prepareDeliveryDate;
        this.shippingDate = shippingDate;
        this.completeDate = completeDate;
        this.orderProducts = orderProducts;
    }

    public void updateOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }
}
