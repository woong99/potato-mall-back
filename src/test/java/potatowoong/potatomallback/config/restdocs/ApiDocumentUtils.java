package potatowoong.potatomallback.config.restdocs;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import java.util.Arrays;
import java.util.Map;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;

public class ApiDocumentUtils {

    /**
     * 공통 요청 필드 생성
     */
    public static OperationRequestPreprocessor getDocumentRequest() {
        return preprocessRequest(
            modifyHeaders()
                .remove("X-CSRF-TOKEN")
                .add("Authorization", "AccessToken"),
            modifyUris()
                .scheme("https")
                .host("docs.api.com")
                .removePort(),
            prettyPrint()
        );
    }

    /**
     * 인증 없는 공통 요청 필드 생성
     */
    public static OperationRequestPreprocessor getNoAuthDocumentRequest() {
        return preprocessRequest(
            modifyHeaders()
                .remove("X-CSRF-TOKEN"),
            prettyPrint()
        );
    }

    /**
     * 공통 응답 필드 생성
     */
    public static OperationResponsePreprocessor getDocumentResponse() {
        return preprocessResponse(
            modifyHeaders()
                .remove("Vary")
                .remove("X-Content-Type-Options")
                .remove("X-XSS-Protection")
                .remove("Cache-Control")
                .remove("Pragma")
                .remove("Expires")
                .remove("X-Frame-Options"),
            prettyPrint());
    }

    /**
     * 커스텀 응답 필드 생성
     */
    public static CustomResponseFieldsSnippet customResponseFields(
        String type,
        PayloadSubsectionExtractor<?> subsectionExtractor,
        Map<String, Object> attributes,
        FieldDescriptor... descriptors) {
        return new CustomResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes, true);
    }

}
