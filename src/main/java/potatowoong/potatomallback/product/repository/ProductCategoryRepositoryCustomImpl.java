package potatowoong.potatomallback.product.repository;

import static com.querydsl.core.types.ExpressionUtils.count;
import static potatowoong.potatomallback.product.entity.QProduct.product;
import static potatowoong.potatomallback.product.entity.QProductCategory.productCategory;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.response.ProductCategoryResDto.ProductCategorySearchResDto;

@RequiredArgsConstructor
public class ProductCategoryRepositoryCustomImpl implements ProductCategoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponseDto<ProductCategorySearchResDto> findProductCategoryWithPage(PageRequestDto pageRequestDto) {
        List<ProductCategorySearchResDto> result = getPagingResult(pageRequestDto);
        final long totalElements = getTotalElements(pageRequestDto);

        return new PageResponseDto<>(result, totalElements);
    }

    /**
     * 페이징 결과 조회
     */
    private List<ProductCategorySearchResDto> getPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(
                    ProductCategorySearchResDto.class,
                    productCategory.productCategoryId,
                    productCategory.name,
                    ExpressionUtils.as(
                        JPAExpressions.select(count(product.productId))
                            .from(product)
                            .where(product.productCategory.productCategoryId.eq(productCategory.productCategoryId)
                            ), "productCount"),
                    productCategory.updatedAt
                ))
            .from(productCategory)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.size())
            .orderBy(productCategory.productCategoryId.desc())
            .fetch();
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getTotalElements(PageRequestDto pageRequestDto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(productCategory.count())
                .from(productCategory)
                .where(getSearchConditions(pageRequestDto))
                .fetchOne())
            .orElse(0L);
    }

    /**
     * 검색 조건
     */
    private BooleanBuilder getSearchConditions(PageRequestDto pageRequestDto) {
        final String searchWord = pageRequestDto.searchWord();
        BooleanBuilder builder = new BooleanBuilder();

        return builder
            .and(productCategory.name.contains(searchWord));
    }
}
