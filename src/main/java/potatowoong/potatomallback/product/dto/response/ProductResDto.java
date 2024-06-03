package potatowoong.potatomallback.product.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import potatowoong.potatomallback.file.entity.AtchFile;
import potatowoong.potatomallback.file.enums.S3Folder;
import potatowoong.potatomallback.product.entity.Product;
import potatowoong.potatomallback.utils.S3Utils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductResDto {

    @Builder
    public record ProductSearchResDto(
        Long productId,
        String name,
        int price,
        int stockQuantity,
        String categoryName,
        String thumbnailUrl,
        LocalDateTime updatedAt
    ) {

        public ProductSearchResDto(Long productId, String name, int price, int stockQuantity, String categoryName, String thumbnailUrl, LocalDateTime updatedAt) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.categoryName = categoryName;
            this.thumbnailUrl = S3Utils.getS3FileUrl() + S3Folder.PRODUCT.getFolderName() + "/" + thumbnailUrl;
            this.updatedAt = updatedAt;
        }
    }

    @Builder
    public record ProductDetailResDto(
        Long productId,
        String name,
        String description,
        int price,
        int stockQuantity,
        Long productCategoryId,
        String thumbnailUrl,
        Long thumbnailFileId
    ) {

        public static ProductDetailResDto of(Product product) {
            AtchFile atchFile = product.getThumbnailFile();
            return ProductDetailResDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getContent())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .productCategoryId(product.getProductCategory().getProductCategoryId())
                .thumbnailUrl(S3Utils.getS3FileUrl() + atchFile.getS3Folder() + "/" + atchFile.getStoredFileName())
                .thumbnailFileId(atchFile.getAtchFileId())
                .build();
        }
    }
}
