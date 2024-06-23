package potatowoong.potatomallback.domain.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import potatowoong.potatomallback.domain.auth.entity.Member;

@Entity
@Table(name = "product_like", uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "user_id"})})
@Comment("상품 좋아요 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("상품 좋아요 정보 IDX")
    private Long productLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @Comment("상품 정보 IDX")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @Comment("사용자 ID")
    private Member member;

    @Builder
    public ProductLike(Product product, Member member) {
        this.product = product;
        this.member = member;
    }
}
