package potatowoong.potatomallback.domain.ranking.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.domain.ranking.dto.SearchLogResDto;
import potatowoong.potatomallback.domain.ranking.dto.SearchSnapshotResDto.SearchRankResDto;
import potatowoong.potatomallback.domain.ranking.dto.SearchSnapshotResDto.SearchRankingSnapshotResDto;
import potatowoong.potatomallback.domain.ranking.entity.SearchKeywordRanking;
import potatowoong.potatomallback.domain.ranking.enums.RankState;
import potatowoong.potatomallback.domain.ranking.repository.AccessLogRepository;
import potatowoong.potatomallback.domain.ranking.repository.SearchKeywordRankingRepository;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final AccessLogRepository accessLogRepository;

    private final SearchKeywordRankingRepository searchKeywordRankingRepository;

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    @Cacheable(value = "recentTop10SearchKeyword", key = "#now")
    public SearchRankingSnapshotResDto getRecentTop10SearchKeyword(LocalDateTime now) {
        List<SearchRankResDto> searchRankResDtos = searchKeywordRankingRepository.findAllByParsedAt(now).stream()
            .map(SearchRankResDto::of)
            .toList();
        return SearchRankingSnapshotResDto.builder()
            .searchRankResDtos(searchRankResDtos)
            .searchTime(now.format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")))
            .build();
    }

    @Transactional
    public void parseRecentTop10SearchKeyword() {
        // 현재 시간 계산
        LocalDateTime calculatedNowTime = getCalculatedNowTime();

        // 1시간 전 시간 계산
        LocalDateTime before1HourTime = calculatedNowTime.minusHours(1);

        // 이전 데이터 시간 계산
        LocalDateTime calculatedBeforeTime = getCalculatedBeforeTime(calculatedNowTime);

        // 검색 로그 조회
        List<SearchLogResDto> recentTop10SearchKeywords = getRecentTop10SearchKeyword(calculatedNowTime, before1HourTime);

        // 이전 시간대 데이터 조회
        Map<String, Integer> beforeRecentTop10SearchKeywords = searchKeywordRankingRepository.findAllByParsedAt(calculatedBeforeTime).stream()
            .collect(Collectors.toMap(SearchKeywordRanking::getKeyword, SearchKeywordRanking::getRank));

        // 검색어 순위 업데이트
        for (SearchLogResDto dto : recentTop10SearchKeywords) {
            calculateRankState(dto, beforeRecentTop10SearchKeywords.get(dto.getKeyword()));
        }

        // 검색어 순위 파싱 결과 저장
        List<SearchKeywordRanking> searchKeywordRankings = recentTop10SearchKeywords.stream()
            .map(dto -> {
                SearchKeywordRanking ranking = SearchKeywordRanking.of(dto);
                ranking.updateParsedAt(calculatedNowTime);
                return ranking;
            })
            .toList();

        // 검색어 순위 파싱 결과 저장
        bulkInsertSearchKeywordRanking(searchKeywordRankings);
    }

    /**
     * 현재 시간을 10분 단위로 자른 시간고 올림하여 반환 Ex) 2024-06-16 12:38:56 -> 2024-06-16 12:40:00
     */
    private LocalDateTime getCalculatedNowTime() {
        LocalDateTime now = LocalDateTime.now();
        int nowMinute = now.getMinute();

        int calculatedMinute = (nowMinute / 10 + 1) * 10;
        if (calculatedMinute >= 60) {
            now = now.plusHours(1).withMinute(0);
        } else {
            now = now.withMinute(calculatedMinute);
        }

        now = now.withSecond(0).withNano(0);
        return now;
    }

    /**
     * 현재 시간을 10분 단위로 자르고 10분을 빼서 반환 Ex) 2024-06-16 12:38:56 -> 2024-06-16 12:30:00
     */
    private LocalDateTime getCalculatedBeforeTime(LocalDateTime now) {
        int nowMinute = now.getMinute();

        int calculatedMinute = (nowMinute / 10 - 1) * 10;

        return calculatedMinute < 0 ? now.minusHours(1).withMinute(50) : now.withMinute(calculatedMinute);
    }

    /**
     * 현재 시간과 1시간 전 시간을 기준으로 검색 로그를 조회하여 최근 10개의 검색어 통계를 반환
     */
    private List<SearchLogResDto> getRecentTop10SearchKeyword(LocalDateTime calculatedNowTime, LocalDateTime before1HourTime) {
        // 현재 시간과 1시간 전 시간을 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
        String parsedNowTime = calculatedNowTime.format(formatter);
        String parsedBefore1HourTime = before1HourTime.format(formatter);

        return accessLogRepository.getRecentTop10SearchKeyword(parsedNowTime, parsedBefore1HourTime);
    }

    /**
     * 검색어 순위 상태 계산
     */
    private void calculateRankState(SearchLogResDto dto, Integer beforeRank) {
        int nowRank = dto.getRank();

        if (beforeRank == null) {
            dto.updateRankState(RankState.NEW);
        } else if (beforeRank > nowRank) {
            dto.updateRankState(RankState.UP);
        } else if (beforeRank < nowRank) {
            dto.updateRankState(RankState.DOWN);
        } else {
            dto.updateRankState(RankState.SAME);
        }
    }

    /**
     * 검색어 순위 파싱 결과를 bulk insert
     */
    private void bulkInsertSearchKeywordRanking(List<SearchKeywordRanking> searchKeywordRankings) {
        final String sql = "INSERT INTO search_keyword_ranking (keyword, rank, rank_state, parsed_at, search_count) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@Nonnull PreparedStatement ps, int i) throws SQLException {
                SearchKeywordRanking ranking = searchKeywordRankings.get(i);
                ps.setString(1, ranking.getKeyword());
                ps.setInt(2, ranking.getRank());
                ps.setString(3, ranking.getRankState().name());
                ps.setTimestamp(4, Timestamp.valueOf(ranking.getParsedAt()));
                ps.setLong(5, ranking.getSearchCount());
            }

            @Override
            public int getBatchSize() {
                return searchKeywordRankings.size();
            }
        });
    }
}
