package com.teadelivery.ordercatalog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AWS S3 and CloudFront configuration properties.
 * Maps to aws.s3.* and aws.cloudfront.* in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
public class AwsStorageProperties {

    private S3Properties s3 = new S3Properties();
    private CloudFrontProperties cloudfront = new CloudFrontProperties();
    private SqsQueuesProperties sqs = new SqsQueuesProperties();

    @Getter
    @Setter
    public static class S3Properties {
        private String region = "us-east-1";
        private BucketsProperties buckets = new BucketsProperties();

        @Getter
        @Setter
        public static class BucketsProperties {
            private String media = "nashtto-media-prod";
            private String documents = "nashtto-documents-prod";
        }
    }

    @Getter
    @Setter
    public static class CloudFrontProperties {
        private String distributionId;
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class SqsQueuesProperties {
        private QueuesProperties queues = new QueuesProperties();

        @Getter
        @Setter
        public static class QueuesProperties {
            private String imageProcessing = "nashtto-image-processing-queue-prod";
        }
    }
}
