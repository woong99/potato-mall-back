package potatowoong.potatomallback.domain.review.entity;


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
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.global.config.db.BaseEntity;

@Entity
@Comment("리뷰 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("리뷰 정보 IDX")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @Comment("상품 정보 IDX")

    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    @Comment("사용자 ID")
    private Member member;

    @Column(nullable = false, length = 1000)
    @Comment("내용")
    private String contents;

    @Column(nullable = false)
    @Comment("별점")
    private int score;

    @Builder
    public Review(Product product, Member member, String contents, int score) {
        this.product = product;
        this.member = member;
        this.contents = contents;
        this.score = score;
    }

    public static Review of(UserReviewReqDto.Add dto, Product product, Member member) {
        return Review.builder()
            .product(product)
            .member(member)
            .contents(dto.contents())
            .score(dto.score())
            .build();
    }

    public void modifyReview(UserReviewReqDto.Modify dto) {
        this.contents = dto.contents();
        this.score = dto.score();
    }
}
