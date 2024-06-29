package potatowoong.potatomallback.domain.cart.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.product.entity.Product;
import potatowoong.potatomallback.global.config.db.BaseEntity;

@Entity
@Comment("장바구니 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingCart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("장바구니 정보 IDX")
    private Long shoppingCartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @Comment("상품 정보 IDX")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @Comment("사용자 ID")
    private Member member;

    @Column(nullable = false)
    @Comment("수량")
    private int quantity;

    @Builder
    public ShoppingCart(Product product, Member member, int quantity) {
        this.product = product;
        this.member = member;
        this.quantity = quantity;
    }

    public void modifyShoppingCart(final int quantity) {
        this.quantity = quantity;
    }
}
