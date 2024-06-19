package potatowoong.potatomallback.product.document;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "product_name")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setting(settingPath = "/elastic/elastic-setting.json")
@Mapping(mappingPath = "/elastic/elastic-mapping.json")
public class ProductNameDocument {

    @Id
    private String id;

    @Field(name = "name", type = FieldType.Text)
    private String name;

    @Builder
    public ProductNameDocument(String name) {
        this.name = name;
    }

    public void modifyName(String name) {
        this.name = name;
    }
}
