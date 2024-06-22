package potatowoong.potatomallback.domain.ranking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import potatowoong.potatomallback.domain.ranking.enums.RankState;
import potatowoong.potatomallback.domain.ranking.dto.SearchLogResDto;

@Entity
@Comment("실시간 검색어 순위 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeywordRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("실시간 검색어 순위 정보 IDX")
    private Long searchKeywordRankingId;

    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    @Comment("파싱 일시")
    private LocalDateTime parsedAt;

    @Column(nullable = false, updatable = false, length = 200)
    @Comment("검색어")
    private String keyword;

    @Column(nullable = false, updatable = false)
    @Comment("순위")
    private int rank;

    @Column(nullable = false, updatable = false)
    @Comment("검색 횟수")
    private long searchCount;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Comment("상태(UP: 순위 상승, DOWN: 순위 하락, NEW: 신규, SAME: 순위 변동 없음)")
    private RankState rankState;

    @Builder
    public SearchKeywordRanking(LocalDateTime parsedAt, String keyword, int rank, long searchCount, RankState rankState) {
        this.parsedAt = parsedAt;
        this.keyword = keyword;
        this.rank = rank;
        this.searchCount = searchCount;
        this.rankState = rankState;
    }

    public static SearchKeywordRanking of(SearchLogResDto dto) {
        return SearchKeywordRanking.builder()
            .keyword(dto.getKeyword())
            .rank(dto.getRank())
            .searchCount(dto.getCount())
            .rankState(dto.getRankState())
            .build();
    }

    public void updateParsedAt(LocalDateTime parsedAt) {
        this.parsedAt = parsedAt;
    }
}
