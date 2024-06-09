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
import potatowoong.potatomallback.product.dto.response.ProductResDto.UserProductSearchResDto;

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
    public PageResponseDto<UserProductSearchResDto> findUserProductWithPage(PageRequestDto pageRequestDto) {
        List<UserProductSearchResDto> result = getUserProductPagingResult(pageRequestDto);
        final long totalElements = getProductTotalElements(pageRequestDto);

        return new PageResponseDto<>(result, totalElements);
    }

    /**
     * 페이징 결과 조회
     */
    private List<ProductSearchResDto> getProductPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(ProductSearchResDto.class, product.productId, product.name, product.price, product.stockQuantity, product.productCategory.name, product.thumbnailFile.storedFileName, product.updatedAt))
            .from(product)
            .leftJoin(product.thumbnailFile, atchFile)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.size())
            .orderBy(getProductOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 사용자 - 페이징 결과 조회
     */
    private List<UserProductSearchResDto> getUserProductPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(UserProductSearchResDto.class, product.productId, product.name, product.price, product.thumbnailFile.storedFileName))
            .from(product)
            .leftJoin(product.thumbnailFile, atchFile)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.size())
            .orderBy(getUserProductOrderConditions(pageRequestDto))
            .fetch();
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
            .and(StringUtils.isNotBlank(pageRequestDto.searchWord()) ? product.name.contains(pageRequestDto.searchWord()) : null);
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getProductOrderConditions(PageRequestDto pageRequestDto) {
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

    /**
     * 사용자 - 정렬 조건
     */
    private OrderSpecifier<?> getUserProductOrderConditions(PageRequestDto pageRequestDto) {
        return switch (pageRequestDto.sortCondition()) {
            case "lowPrice" -> product.price.asc();
            case "highPrice" -> product.price.desc();
            // TODO : 판매량 순 추가 예정
            case "latest" -> product.createdAt.desc();
            default -> product.productId.desc();
        };
    }
}
