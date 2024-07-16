package potatowoong.potatomallback.domain.pay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import potatowoong.potatomallback.domain.product.entity.Product;

@Entity
@Comment("결제 트랜잭션 정보")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("결제 트랜잭션 정보 IDX")
    private Long payTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @Comment("주문 ID")
    private TossPaymentPayTransaction tossPaymentPayTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @Comment("상품 정보 IDX")
    private Product product;

    @Column(nullable = false, updatable = false)
    @Comment("상품 가격")
    private int price;

    @Column(nullable = false, updatable = false)
    @Comment("구매량")
    private int quantity;

    @Builder
    public PayTransaction(TossPaymentPayTransaction tossPaymentPayTransaction, Product product, int price, int quantity) {
        this.tossPaymentPayTransaction = tossPaymentPayTransaction;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
    }
}
