package potatowoong.potatomallback.domain.ranking.repository;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.json.JsonData;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import potatowoong.potatomallback.domain.ranking.document.SearchKeywordDocument;
import potatowoong.potatomallback.domain.ranking.dto.SearchLogResDto;

@Repository
@RequiredArgsConstructor
public class AccessLogRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<SearchLogResDto> getRecentTop10SearchKeyword(String nowTime, String before1HourTime) {
        // 집계 쿼리 생성
        Aggregation aggregation = AggregationBuilders.terms()
            .field("query_params.searchWord.keyword")
            .build()
            ._toAggregation();

        // 조회 쿼리 생성
        NativeQuery query = NativeQuery
            .builder()
            .withQuery(q ->
                q.bool(v ->
                    v.must(m ->
                            m.match(t ->
                                t.field("request")
                                    .query("/api/product/search")
                            )
                        )
                        .must(m ->
                            m.match(t ->
                                t.field("method")
                                    .query("GET")
                            )
                        ).must(m ->
                            m.exists(t ->
                                t.field("query_string")
                            )
                        ).must(m ->
                            m.range(t ->
                                t.field("timestamp.keyword")
                                    .gte(JsonData.fromJson("\"" + before1HourTime + "\""))
                                    .lte(JsonData.fromJson("\"" + nowTime + "\""))
                            )
                        ).mustNot(m ->
                            m.exists(t ->
                                t.field("query_params.page")
                            )
                        )
                )
            )
            .withFields("query_params.searchWord")
            .withSort(s ->
                s.field(v ->
                    v.field("timestamp.keyword")
                        .order(SortOrder.Desc)
                )
            )
            .withAggregation("keyword_agg", aggregation)
            .withTrackScores(true)
            .build();
        
        SearchHits<SearchKeywordDocument> searchHits = elasticsearchOperations.search(query, SearchKeywordDocument.class);

        ElasticsearchAggregations aggregations = (ElasticsearchAggregations) searchHits.getAggregations();
        if (aggregations == null) {
            return Collections.emptyList();
        }

        List<ElasticsearchAggregation> aggregationList = aggregations.aggregations();
        Buckets<StringTermsBucket> buckets = aggregationList.get(0)
            .aggregation()
            .getAggregate()
            .sterms()
            .buckets();
        List<StringTermsBucket> bucketList = buckets.array();

        return IntStream.range(0, bucketList.size())
            .mapToObj(i -> {
                StringTermsBucket bucket = bucketList.get(i);
                return SearchLogResDto.builder()
                    .keyword(bucket.key().stringValue())
                    .count(bucket.docCount())
                    .rank(i + 1)
                    .build();
            })
            .toList();
    }
}
