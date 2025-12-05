package com.teadelivery.ordercatalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * AWS SDK configuration for SNS/SQS clients.
 * Only enabled when search sync features are enabled.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * SNS client for publishing search index events.
     */
    @Bean
    @ConditionalOnProperty(name = "features.sns.search-sync.enabled", havingValue = "true")
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * SQS client for consuming search index events.
     * Note: SqsClient is also used by spring-cloud-aws-sqs internally
     */
    @Bean
    @ConditionalOnProperty(name = "features.sqs.search-sync.enabled", havingValue = "true")
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
