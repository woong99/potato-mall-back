package potatowoong.potatomallback.domain.ranking.document;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Document(indexName = "access-log-*")
@Getter
@ToString
public class SearchKeywordDocument {

    @Id
    private String id;

    @Field(name = "query_params.searchWord")
    private String searchWord;
}
