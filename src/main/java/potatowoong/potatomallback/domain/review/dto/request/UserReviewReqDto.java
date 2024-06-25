package potatowoong.potatomallback.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.query.SortDirection;
import potatowoong.potatomallback.global.common.PageRequestDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserReviewReqDto {

    /**
     * 리뷰 목록 조회 요청 DTO
     */
    @Getter
    @ToString
    public static class Search extends PageRequestDto {

        @NotNull(message = "상품 ID는 필수 값입니다.")
        private final Long productId;

        public Search(String searchWord, String searchCondition, Integer page, Integer size, String sortCondition, SortDirection sortDirection, Long productId) {
            super(searchWord, searchCondition, page, size, sortCondition, sortDirection);
            this.productId = productId;
        }
    }

    /**
     * 리뷰 등록 요청 DTO
     */
    @Builder
    public record Add(
        @NotNull(message = "상품 ID는 필수 값입니다.")
        Long productId,

        @NotNull(message = "리뷰 내용을 입력해주세요.")
        @Size(max = 1000, message = "리뷰 내용은 최대 1000자까지 입력 가능합니다.")
        String contents,

        @NotNull(message = "별점을 입력해주세요.")
        @Max(value = 5, message = "별점은 5점까지만 입력 가능합니다.")
        @Min(value = 1, message = "별점은 1점부터 입력 가능합니다.")
        Integer score
    ) {

    }

    /**
     * 리뷰 수정 요청 DTO
     */
    @Builder
    public record Modify(
        @NotNull(message = "리뷰 ID는 필수 값입니다.")
        Long reviewId,

        @NotNull(message = "리뷰 내용을 입력해주세요.")
        @Size(max = 1000, message = "리뷰 내용은 최대 1000자까지 입력 가능합니다.")
        String contents,

        @NotNull(message = "별점을 입력해주세요.")
        @Max(value = 5, message = "별점은 5점까지만 입력 가능합니다.")
        @Min(value = 1, message = "별점은 1점부터 입력 가능합니다.")
        Integer score
    ) {

    }
}
