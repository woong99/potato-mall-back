package potatowoong.potatomallback.domain.review.repository;

import static potatowoong.potatomallback.domain.review.entity.QReview.review;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.SortDirection;
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 사용자 - 리뷰 목록 조회
     *
     * @param dto 페이지 요청 DTO
     * @return 리뷰 목록 + 총 리뷰 수
     */
    @Override
    public PageResponseDto<UserReviewResDto.Detail> findReviewWithPage(UserReviewReqDto.Search dto) {
        return new PageResponseDto<>(getPagingResult(dto), getTotalElements(dto));
    }

    /**
     * 해당 상품의 리뷰 평점 합계 조회
     *
     * @param productId 상품 ID
     * @return 리뷰 평점 합계
     */
    @Override
    public int sumScoreByProductId(final long productId) {
        return Optional.ofNullable(jpaQueryFactory.select(
                review.score.sum()
            )
            .from(review)
            .where(review.product.productId.eq(productId))
            .fetchOne()).orElse(0);
    }

    /**
     * 사용자 - 페이징 결과 조회
     *
     * @param dto 페이지 요청 DTO
     * @return 리뷰 상세 정보 + 작성자명
     */
    private List<UserReviewResDto.Detail> getPagingResult(UserReviewReqDto.Search dto) {

        return jpaQueryFactory.select(
                Projections.constructor(
                    UserReviewResDto.Detail.class,
                    review.reviewId,
                    review.contents,
                    review.score,
                    review.member.nickname,
                    Expressions.stringTemplate("DATE_FORMAT({0}, {1})", review.createdAt, "%Y-%m-%d")
                ))
            .from(review)
            .where(review.product.productId.eq(dto.getProductId()))
            .innerJoin(review.member)
            .offset(dto.getFirstIndex())
            .limit(dto.getSize())
            .orderBy(getUserReviewOrderConditions(dto))
            .fetch();
    }

    /**
     * 전체 데이터 수 조회
     *
     * @return 전체 데이터 수
     */
    private long getTotalElements(UserReviewReqDto.Search dto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(review.count())
                .where(review.product.productId.eq(dto.getProductId()))
                .from(review)
                .fetchOne())
            .orElse(0L);
    }

    /**
     * 사용자 - 정렬 조건
     * <br> 1. 기본 정렬 조건 : 최신순
     * <br> 2. 평점 순(오름차순, 내림차순)
     *
     * @param dto 페이지 요청 DTO
     * @return 정렬 조건
     */
    private OrderSpecifier<?> getUserReviewOrderConditions(UserReviewReqDto.Search dto) {
        if (StringUtils.isBlank(dto.getSortCondition())) {
            return review.createdAt.desc();
        }

        if (dto.getSortCondition().equals("score")) {
            return dto.getSortDirection().equals(SortDirection.ASCENDING) ? review.score.asc() : review.score.desc();
        }
        return review.createdAt.desc();
    }
}
