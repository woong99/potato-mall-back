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
@Comment("구매 내역 정보 - 상품 정보 연결 테이블")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("구매 내역 정보 - 상품 정보 연결 테이블 IDX")
    private Long orderProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @Comment("상품 정보 IDX")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_history_id", nullable = false, updatable = false)
    @Comment("구매 내역 정보 IDX")
    private OrderHistory orderHistory;

    @Column(nullable = false, updatable = false)
    @Comment("상품 수량")
    private int quantity;

    @Column(nullable = false, updatable = false)
    @Comment("결제 금액")
    private int amount;

    @Builder
    public OrderProduct(Product product, OrderHistory orderHistory, int quantity, int amount) {
        this.product = product;
        this.orderHistory = orderHistory;
        this.quantity = quantity;
        this.amount = amount;
    }
}
