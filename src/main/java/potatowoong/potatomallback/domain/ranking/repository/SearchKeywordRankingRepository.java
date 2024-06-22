package potatowoong.potatomallback.domain.ranking.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.ranking.entity.SearchKeywordRanking;

public interface SearchKeywordRankingRepository extends JpaRepository<SearchKeywordRanking, Long> {

    List<SearchKeywordRanking> findAllByParsedAt(LocalDateTime parsedAt);
}
