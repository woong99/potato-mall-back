package potatowoong.potatomallback.domain.ranking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import potatowoong.potatomallback.domain.ranking.enums.RankState;

@Getter
@ToString
@Builder
public class SearchLogResDto {

    private String keyword;

    private long count;

    private int rank;

    private RankState rankState;

    public void updateRankState(RankState rankState) {
        this.rankState = rankState;
    }
}
