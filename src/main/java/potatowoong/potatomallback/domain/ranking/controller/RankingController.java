package potatowoong.potatomallback.domain.ranking.controller;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.ranking.dto.SearchSnapshotResDto.SearchRankingSnapshotResDto;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.domain.ranking.service.RankingService;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 실시간 검색어 Top10 조회 API
     */
    @GetMapping("/recent-top-10-search-keyword")
    public ApiResponseEntity<SearchRankingSnapshotResDto> recentTop10SearchKeyword() {
        return ApiResponseEntity.of(rankingService.getRecentTop10SearchKeyword(getNowTime()));
    }

    /**
     * 현재 시간을 10분 단위로 자른 시간을 반환 Ex) 2021-08-10 12:34:56 -> 2021-08-10 12:30:00
     */
    private LocalDateTime getNowTime() {
        LocalDateTime now = LocalDateTime.now();
        int minute = now.getMinute();
        now = now.minusMinutes(minute % 10);
        now = now.withSecond(0);
        now = now.withNano(0);
        return now;
    }
}
