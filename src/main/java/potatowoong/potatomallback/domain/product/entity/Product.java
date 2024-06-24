package potatowoong.potatomallback.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import potatowoong.potatomallback.domain.file.entity.AtchFile;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto.ProductAddReqDto;
import potatowoong.potatomallback.domain.product.dto.request.ProductReqDto.ProductModifyReqDto;
import potatowoong.potatomallback.global.config.db.BaseEntity;

@Entity
@Comment("상품 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("상품 정보 IDX")
    private Long productId;

    @Column(nullable = false, length = 100)
    @Comment("상품명")
    private String name;

    @Column(nullable = false)
    @Comment("가격")
    private int price;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    @Comment("상품 설명")
    private String content;

    @Column(nullable = false)
    @Comment("재고 수량")
    private int stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    @Comment("상품 카테고리 정보 IDX")
    private ProductCategory productCategory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atch_file_id")
    @Comment("썸네일 이미지 정보 IDX")
    private AtchFile thumbnailFile;

    @OneToMany(mappedBy = "product")
    private List<ProductLike> productLikes;

    @Builder
    public Product(String name, int price, String content, int stockQuantity, ProductCategory productCategory, AtchFile thumbnailFile) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.stockQuantity = stockQuantity;
        this.productCategory = productCategory;
        this.thumbnailFile = thumbnailFile;
    }

    public static Product addOf(ProductAddReqDto dto, ProductCategory productCategory, AtchFile thumbnailFile) {
        return Product.builder()
            .name(dto.name())
            .price(dto.price())
            .content(dto.content())
            .stockQuantity(dto.stockQuantity())
            .productCategory(productCategory)
            .thumbnailFile(thumbnailFile)
            .build();
    }

    public void changeThumbnailFile(AtchFile thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public void modify(ProductModifyReqDto dto, ProductCategory productCategory) {
        this.name = dto.name();
        this.price = dto.price();
        this.content = dto.content();
        this.stockQuantity = dto.stockQuantity();
        this.productCategory = productCategory;
    }

    public void removeThumbnailFile() {
        this.thumbnailFile = null;
    }
}
