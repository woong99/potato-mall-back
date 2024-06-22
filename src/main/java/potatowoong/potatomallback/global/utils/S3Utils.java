package potatowoong.potatomallback.global.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Utils {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region.static}")
    private String region;

    private static class Holder {

        private static final InnerHolder INNER_HOLDER = new InnerHolder();

        private Holder() {
            super();
        }
    }

    private static final class InnerHolder {

        private String s3FileUrl;

        private void setS3FileUrl(String s3FileUrl) {
            this.s3FileUrl = s3FileUrl;
        }
    }

    @PostConstruct
    public void init() {
        Holder.INNER_HOLDER.setS3FileUrl("https://" + bucket + ".s3." + region + ".amazonaws.com/");
    }

    public static String getS3FileUrl() {
        return Holder.INNER_HOLDER.s3FileUrl;
    }
}
