package potatowoong.potatomallback.common;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.SortDirection;

public record PageRequestDto(
    String searchWord,
    String searchCondition,
    Integer page,
    Integer size,
    String sortCondition,
    SortDirection sortDirection) {

    @Builder
    public PageRequestDto(
        String searchWord,
        String searchCondition,
        Integer page,
        Integer size,
        String sortCondition,
        SortDirection sortDirection) {

        this.searchWord = StringUtils.defaultString(searchWord);
        this.searchCondition = StringUtils.defaultString(searchCondition);
        this.page = page == null ? 0 : page;
        this.size = size == null ? 10 : size;
        this.sortCondition = StringUtils.defaultString(sortCondition);
        this.sortDirection = sortDirection != null ? sortDirection : SortDirection.DESCENDING;
    }

    public long getFirstIndex() {
        return (long) this.page * this.size;
    }
}
