package potatowoong.potatomallback.global.common;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.SortDirection;

@Getter
@ToString
public class PageRequestDto {

    private final String searchWord;

    private final String searchCondition;

    private final Integer page;

    private final Integer size;

    private final String sortCondition;

    private final SortDirection sortDirection;

    @Builder
    public PageRequestDto(String searchWord, String searchCondition, Integer page, Integer size, String sortCondition, SortDirection sortDirection) {
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
