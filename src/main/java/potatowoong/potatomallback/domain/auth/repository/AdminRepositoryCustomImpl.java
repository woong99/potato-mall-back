package potatowoong.potatomallback.domain.auth.repository;

import static potatowoong.potatomallback.domain.auth.entity.QAdmin.admin;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.SortDirection;
import potatowoong.potatomallback.domain.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

@RequiredArgsConstructor
public class AdminRepositoryCustomImpl implements AdminRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponseDto<AdminResDto> findAdminWithPage(PageRequestDto pageRequestDto) {
        List<AdminResDto> result = getPagingResult(pageRequestDto);
        final long totalElements = getTotalElements(pageRequestDto);
        return new PageResponseDto<>(result, totalElements);
    }

    /**
     * 페이징 결과 조회
     */
    private List<AdminResDto> getPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(AdminResDto.class, admin.adminId, admin.name, admin.updatedAt))
            .from(admin)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getTotalElements(PageRequestDto pageRequestDto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(admin.count())
                .from(admin)
                .where(getSearchConditions(pageRequestDto))
                .fetchOne())
            .orElse(0L);
    }

    /**
     * 검색 조건
     */
    private BooleanBuilder getSearchConditions(PageRequestDto pageRequestDto) {
        final String searchCondition = pageRequestDto.getSearchCondition();
        final String searchWord = pageRequestDto.getSearchWord();
        BooleanBuilder builder = new BooleanBuilder();

        return builder
            .and(searchCondition.equals("adminId") ? admin.adminId.contains(searchWord) : null)
            .and(searchCondition.equals("name") ? admin.name.contains(searchWord) : null)
            .and((StringUtils.isBlank(searchCondition) || searchCondition.equals("all")) && StringUtils.isNotBlank(searchWord)
                ? admin.adminId.contains(searchWord).or(admin.name.contains(searchWord))
                : null);
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
        final String sortCondition = pageRequestDto.getSortCondition();
        final SortDirection sortDirection = pageRequestDto.getSortDirection();
        if (StringUtils.isBlank(sortCondition)) {
            return admin.createdAt.desc();
        }

        return switch (sortCondition) {
            case "adminId" -> sortDirection == SortDirection.ASCENDING ? admin.adminId.asc() : admin.adminId.desc();
            case "name" -> sortDirection == SortDirection.ASCENDING ? admin.name.asc() : admin.name.desc();
            case "updatedAt" -> sortDirection == SortDirection.ASCENDING ? admin.updatedAt.asc() : admin.updatedAt.desc();
            default -> admin.createdAt.desc();
        };
    }
}
