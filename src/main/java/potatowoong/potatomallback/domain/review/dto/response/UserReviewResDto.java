package potatowoong.potatomallback.domain.review.dto.response;

import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import potatowoong.potatomallback.domain.review.entity.Review;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.utils.MaskingUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserReviewResDto {

    /**
     * 리뷰 상세 조회 응답 DTO
     */
    @Builder
    public record Detail(
        Long reviewId,
        String contents,
        int score,
        String nickname,
        String createdAt
    ) {

        public Detail(Long reviewId, String contents, int score, String nickname, String createdAt) {
            this.reviewId = reviewId;
            this.contents = contents;
            this.score = score;
            this.nickname = MaskingUtils.masking(nickname);
            this.createdAt = createdAt;
        }

        public static Detail of(Review review) {
            return Detail.builder()
                .reviewId(review.getReviewId())
                .contents(review.getContents())
                .score(review.getScore())
                .nickname(review.getMember().getNickname())
                .createdAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yy-MM-dd")))
                .build();
        }
    }

    /**
     * 리뷰 목록 조회 응답 DTO
     */
    @Builder
    public record Search(
        PageResponseDto<Detail> pageResponseDto,
        double averageScore
    ) {

    }
}
