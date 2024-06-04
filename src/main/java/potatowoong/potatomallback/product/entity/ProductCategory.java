package potatowoong.potatomallback.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import potatowoong.potatomallback.config.db.BaseEntity;
import potatowoong.potatomallback.product.dto.request.ProductCategoryAddReqDto;

@Entity
@Comment("상품 카테고리 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("상품 카테고리 정보 IDX")
    private Long productCategoryId;

    @Column(nullable = false, length = 100)
    @Comment("카테고리명")
    private String name;

    @OneToMany(mappedBy = "productCategory", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Product> products;

    @Builder
    public ProductCategory(String name, List<Product> products) {
        this.name = name;
        this.products = products;
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
