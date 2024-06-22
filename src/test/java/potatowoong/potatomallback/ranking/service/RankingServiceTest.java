package potatowoong.potatomallback.ranking.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import potatowoong.potatomallback.domain.ranking.dto.SearchLogResDto;
import potatowoong.potatomallback.domain.ranking.entity.SearchKeywordRanking;
import potatowoong.potatomallback.domain.ranking.repository.AccessLogRepository;
import potatowoong.potatomallback.domain.ranking.repository.SearchKeywordRankingRepository;
import potatowoong.potatomallback.domain.ranking.service.RankingService;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private AccessLogRepository accessLogRepository;

    @Mock
    private SearchKeywordRankingRepository searchKeywordRankingRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RankingService rankingService;

    @Nested
    @DisplayName("실시간 검색어 파싱")
    class 실시간_검색어_파싱 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            List<SearchLogResDto> recentTop10SearchKeywords = Collections.emptyList();
            List<SearchKeywordRanking> previousRankings = Collections.emptyList();

            given(accessLogRepository.getRecentTop10SearchKeyword(anyString(), anyString())).willReturn(recentTop10SearchKeywords);
            given(searchKeywordRankingRepository.findAllByParsedAt(any())).willReturn(previousRankings);

            // when
            rankingService.parseRecentTop10SearchKeyword();

            // then
            then(accessLogRepository).should().getRecentTop10SearchKeyword(anyString(), anyString());
            then(searchKeywordRankingRepository).should().findAllByParsedAt(any());
            then(jdbcTemplate).should().batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }
    }

    @Nested
    @DisplayName("실시간 검색어 목록 조회")
    class 실시간_검색어_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            List<SearchKeywordRanking> searchKeywordRankings = Collections.emptyList();

            given(searchKeywordRankingRepository.findAllByParsedAt(any())).willReturn(searchKeywordRankings);

            // when
            rankingService.getRecentTop10SearchKeyword(LocalDateTime.now());

            // then
            then(searchKeywordRankingRepository).should().findAllByParsedAt(any());
        }
    }
}