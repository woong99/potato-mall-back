package potatowoong.potatomallback.domain.product.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import potatowoong.potatomallback.domain.file.enums.S3Folder;
import potatowoong.potatomallback.global.utils.S3Utils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProductResDto {

    @Builder
    public record Search(
        Long productId,
        String name,
        int price,
        String thumbnailUrl,
        long likeCount,
        boolean isLike,
        long reviewCount
    ) {

        public Search(Long productId, String name, int price, String thumbnailUrl, long likeCount, boolean isLike, long reviewCount) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.thumbnailUrl = StringUtils.isBlank(thumbnailUrl) ? "" : S3Utils.getS3FileUrl() + S3Folder.PRODUCT.getFolderName() + "/" + thumbnailUrl;
            this.likeCount = likeCount;
            this.isLike = isLike;
            this.reviewCount = reviewCount;
        }
    }

    @Builder
    public record Detail(
        Long productId,
        String name,
        String description,
        int price,
        int stockQuantity,
        String thumbnailUrl,
        long likeCount,
        boolean isLike
    ) {

        public Detail(Long productId, String name, String description, int price, int stockQuantity, String thumbnailUrl, long likeCount, boolean isLike) {
            this.productId = productId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.thumbnailUrl = StringUtils.isBlank(thumbnailUrl) ? "" : S3Utils.getS3FileUrl() + S3Folder.PRODUCT.getFolderName() + "/" + thumbnailUrl;
            this.likeCount = likeCount;
            this.isLike = isLike;
        }
    }
}
