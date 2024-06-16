package potatowoong.potatomallback.ranking.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import potatowoong.potatomallback.ranking.service.RankingService;

@Component
@EnableAsync
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;

    /**
     * 10분마다 실시간 검색어 파싱
     */
    @Async
    @Scheduled(cron = "0 8-59/10 * * * *")
    public void parseRecentTop10SearchKeyword() {
        rankingService.parseRecentTop10SearchKeyword();
    }
}
