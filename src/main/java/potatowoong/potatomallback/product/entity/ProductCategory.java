package potatowoong.potatomallback.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLRestriction;
import potatowoong.potatomallback.config.db.BaseEntity;
import potatowoong.potatomallback.product.dto.request.ProductCategoryAddReqDto;

@Entity
@Comment("상품 카테고리 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("use_flag = 'Y'")
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("상품 카테고리 정보 IDX")
    private Long productCategoryId;

    @Column(nullable = false, length = 100)
    @Comment("카테고리명")
    private String name;

    @Builder
    public ProductCategory(String name) {
        this.name = name;
    }

    public static ProductCategory addOf(ProductCategoryAddReqDto dto) {
        return ProductCategory.builder()
            .name(dto.name())
            .build();
    }

    public void modify(final String name) {
        this.name = name;
    }
}
