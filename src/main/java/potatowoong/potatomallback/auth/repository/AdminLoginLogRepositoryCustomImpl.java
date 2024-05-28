package potatowoong.potatomallback.auth.repository;

import static potatowoong.potatomallback.auth.entity.QAdminLoginLog.adminLoginLog;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;
import org.springframework.util.StringUtils;
import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

@RequiredArgsConstructor
public class AdminLoginLogRepositoryCustomImpl implements AdminLoginLogRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponseDto<AdminLoginLogResDto> findAdminLoginLogWithPage(PageRequestDto pageRequestDto) {
        List<AdminLoginLogResDto> result = getPagingResult(pageRequestDto);
        final long totalElements = getTotalElements(pageRequestDto);
        return new PageResponseDto<>(result, totalElements);
    }

    /**
     * 페이징 결과 조회
     */
    private List<AdminLoginLogResDto> getPagingResult(PageRequestDto pageRequestDto) {
        return jpaQueryFactory.select(
                Projections.constructor(AdminLoginLogResDto.class, adminLoginLog.adminLoginLogId, adminLoginLog.adminId, adminLoginLog.tryIp, adminLoginLog.tryResult, adminLoginLog.tryDate))
            .from(adminLoginLog)
            .where(getSearchConditions(pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getFirstIndex() + pageRequestDto.size())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getTotalElements(PageRequestDto pageRequestDto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(adminLoginLog.count())
                .from(adminLoginLog)
                .where(getSearchConditions(pageRequestDto))
                .fetchOne())
            .orElse(0L);
    }

    /**
     * 검색 조건
     */
    private BooleanBuilder getSearchConditions(PageRequestDto pageRequestDto) {
        final String searchCondition = pageRequestDto.searchCondition();
        final String searchWord = pageRequestDto.searchWord();
        BooleanBuilder builder = new BooleanBuilder();

        return builder
            .and(searchCondition.equals("adminId") ? adminLoginLog.adminId.contains(searchWord) : null)
            .and(searchCondition.equals("tryIp") ? adminLoginLog.tryIp.contains(searchWord) : null)
            .and(!StringUtils.hasText(searchCondition) && StringUtils.hasLength(searchWord)
                ? adminLoginLog.adminId.contains(searchWord).or(adminLoginLog.tryIp.contains(searchWord))
                : null);
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
        final String sortCondition = pageRequestDto.sortCondition();
        final SortDirection sortDirection = pageRequestDto.sortDirection();
        if (!StringUtils.hasText(sortCondition)) {
            return adminLoginLog.tryDate.desc();
        }

        return switch (sortCondition) {
            case "tryDate" -> sortDirection == SortDirection.ASCENDING ? adminLoginLog.tryDate.asc() : adminLoginLog.tryDate.desc();
            case "adminId" -> sortDirection == SortDirection.ASCENDING ? adminLoginLog.adminId.asc() : adminLoginLog.adminId.desc();
            case "tryResult" -> sortDirection == SortDirection.ASCENDING ? adminLoginLog.tryResult.asc() : adminLoginLog.tryResult.desc();
            default -> adminLoginLog.tryDate.desc();
        };
    }
}
