package potatowoong.potatomallback.domain.product.repository;

import static potatowoong.potatomallback.domain.file.entity.QAtchFile.atchFile;
import static potatowoong.potatomallback.domain.product.entity.QProduct.product;
import static potatowoong.potatomallback.domain.product.entity.QProductLike.productLike;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.SortDirection;
import potatowoong.potatomallback.domain.product.dto.response.ProductResDto.ProductSearchResDto;
import potatowoong.potatomallback.domain.product.dto.response.UserProductResDto;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.utils.SecurityUtils;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponseDto<ProductSearchResDto> findProductWithPage(PageRequestDto pageRequestDto) {
        List<ProductSearchResDto> result = getProductPagingResult(pageRequestDto);
        final long totalElements = getProductTotalElements(pageRequestDto);

        return new PageResponseDto<>(result, totalElements);
    }

    @Override
    public PageResponseDto<UserProductResDto.Search> findUserProductWithPage(PageRequestDto pageRequestDto) {
        List<UserProductResDto.Search> result = getUserProductPagingResult(pageRequestDto);
        final long totalElements = getProductTotalElements(pageRequestDto);

        return new PageResponseDto<>(result, totalElements);
    }

    @Override
    public Optional<UserProductResDto.Detail> findUserProductById(long productId) {
        return Optional.ofNullable(getUserProductDetailResult(productId));
    }

    /**
     * 관리자 - 페이징 결과 조회
     *
     * @param pageRequestDto 페이징 요청 DTO
     * @return 상품 상세 정보 + 썸네일 URL
     */
    private List<ProductSearchResDto> getProductPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(ProductSearchResDto.class, product.productId, product.name, product.price, product.stockQuantity, product.productCategory.name, product.thumbnailFile.storedFileName, product.updatedAt))
            .from(product)
            .leftJoin(product.thumbnailFile, atchFile)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getProductOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 사용자 - 페이징 결과 조회
     *
     * @param pageRequestDto 페이징 요청 DTO
     * @return 상품 상세 정보 + 썸네일 URL + 좋아요 개수 + 좋아요 여부(로그인 시)
     */
    private List<UserProductResDto.Search> getUserProductPagingResult(PageRequestDto pageRequestDto) {
        final String userId = SecurityUtils.getCurrentUserId();

        JPAQuery<UserProductResDto.Search> query = jpaQueryFactory.select(
                Projections.constructor(
                    UserProductResDto.Search.class,
                    product.productId,
                    product.name,
                    product.price,
                    product.thumbnailFile.storedFileName,
                    getLikeCountQuery(),
                    getIsLikeCaseBuilder(userId)
                )
            )
            .from(product)
            .leftJoin(product.thumbnailFile, atchFile)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getUserProductOrderConditions(pageRequestDto));

        if (StringUtils.isNotBlank(userId)) {
            query.leftJoin(product.productLikes, productLike).on(productLike.member.userId.eq(userId));
        }

        return query.fetch();
    }

    /**
     * 사용자 - 상품 상세 조회
     *
     * @param productId 상품 ID
     * @return 상품 상세 정보 + 썸네일 URL + 좋아요 개수 + 좋아요 여부(로그인 시)
     */
    private UserProductResDto.Detail getUserProductDetailResult(final long productId) {
        final String userId = SecurityUtils.getCurrentUserId();

        JPAQuery<UserProductResDto.Detail> query = jpaQueryFactory.select(
                Projections.constructor(
                    UserProductResDto.Detail.class,
                    product.productId,
                    product.name,
                    product.content,
                    product.price,
                    product.stockQuantity,
                    product.thumbnailFile.storedFileName,
                    getLikeCountQuery(),
                    getIsLikeCaseBuilder(userId)
                )
            )
            .from(product)
            .leftJoin(product.thumbnailFile, atchFile)
            .where(product.productId.eq(productId));

        if (StringUtils.isNotBlank(userId)) {
            query.leftJoin(product.productLikes, productLike).on(productLike.member.userId.eq(userId));
        }

        return query.fetchOne();
    }

    /**
     * 좋아요 개수 조회 서브 쿼리
     */
    private JPQLQuery<Long> getLikeCountQuery() {
        return JPAExpressions.select(productLike.count())
            .from(productLike)
            .where(productLike.product.eq(product));
    }

    /**
     * 사용자 - 좋아요 여부 CaseBuilder
     */
    private BooleanExpression getIsLikeCaseBuilder(final String userId) {
        return StringUtils.isNotBlank(userId) ? new CaseBuilder()
            .when(productLike.product.productId.isNotNull())
            .then(true)
            .otherwise(false) :
            Expressions.asBoolean(false);
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getProductTotalElements(PageRequestDto pageRequestDto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(product.count())
                .from(product)
                .where(getSearchConditions(pageRequestDto))
                .fetchOne())
            .orElse(0L);
    }

    /**
     * 검색 조건
     */
    private BooleanBuilder getSearchConditions(PageRequestDto pageRequestDto) {
        BooleanBuilder builder = new BooleanBuilder();

        return builder
            .and(StringUtils.isNotBlank(pageRequestDto.getSearchWord()) ? product.name.contains(pageRequestDto.getSearchWord()) : null);
    }

    /**
     * 관리자 페이징 - 정렬 조건
     */
    private OrderSpecifier<?> getProductOrderConditions(PageRequestDto pageRequestDto) {
        final String sortCondition = pageRequestDto.getSortCondition();
        final SortDirection sortDirection = pageRequestDto.getSortDirection();
        if (StringUtils.isBlank(sortCondition)) {
            return product.productId.desc();
        }

        return switch (sortCondition) {
            case "name" -> sortDirection == SortDirection.ASCENDING ? product.name.asc() : product.name.desc();
            case "price" -> sortDirection == SortDirection.ASCENDING ? product.price.asc() : product.price.desc();
            case "categoryName" -> sortDirection == SortDirection.ASCENDING ? product.productCategory.name.asc() : product.productCategory.name.desc();
            case "stockCount" -> sortDirection == SortDirection.ASCENDING ? product.stockQuantity.asc() : product.stockQuantity.desc();
            default -> product.productId.desc();
        };
    }

    /**
     * 사용자 페이징 - 정렬 조건
     */
    private OrderSpecifier<?> getUserProductOrderConditions(PageRequestDto pageRequestDto) {
        return switch (pageRequestDto.getSortCondition()) {
            case "lowPrice" -> product.price.asc();
            case "highPrice" -> product.price.desc();
            // TODO : 판매량 순 추가 예정
            case "latest" -> product.createdAt.desc();
            default -> product.productId.desc();
        };
    }
}
