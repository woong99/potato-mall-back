package potatowoong.potatomallback.domain.ranking.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import potatowoong.potatomallback.domain.ranking.entity.SearchKeywordRanking;
import potatowoong.potatomallback.domain.ranking.enums.RankState;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchSnapshotResDto {

    @Builder
    public record SearchRankResDto(
        String keyword,
        int rank,
        RankState rankState) {

        public static SearchRankResDto of(SearchKeywordRanking ranking) {
            return SearchRankResDto.builder()
                .keyword(ranking.getKeyword())
                .rank(ranking.getRank())
                .rankState(ranking.getRankState())
                .build();
        }
    }

    @Builder
    public record SearchRankingSnapshotResDto(
        String searchTime,
        List<SearchRankResDto> searchRankResDtos
    ) {

    }
}
