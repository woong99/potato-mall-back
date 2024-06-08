package potatowoong.potatomallback.product.repository;

import static potatowoong.potatomallback.file.entity.QAtchFile.atchFile;
import static potatowoong.potatomallback.product.entity.QProduct.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.SortDirection;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.product.dto.response.ProductResDto.ProductSearchResDto;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponseDto<ProductSearchResDto> findProductWithPage(PageRequestDto pageRequestDto) {
        List<ProductSearchResDto> result = getPagingResult(pageRequestDto);
        final long totalElements = getTotalElements(pageRequestDto);

        return new PageResponseDto<>(result, totalElements);
    }

    /**
     * 페이징 결과 조회
     */
    private List<ProductSearchResDto> getPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(ProductSearchResDto.class, product.productId, product.name, product.price, product.stockQuantity, product.productCategory.name, product.thumbnailFile.storedFileName, product.updatedAt))
            .from(product)
            .leftJoin(product.thumbnailFile, atchFile)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.size())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getTotalElements(PageRequestDto pageRequestDto) {
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
            .and(StringUtils.isNotBlank(pageRequestDto.searchWord()) ? product.name.contains(pageRequestDto.searchWord()) : null);
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
        final String sortCondition = pageRequestDto.sortCondition();
        final SortDirection sortDirection = pageRequestDto.sortDirection();
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
}
