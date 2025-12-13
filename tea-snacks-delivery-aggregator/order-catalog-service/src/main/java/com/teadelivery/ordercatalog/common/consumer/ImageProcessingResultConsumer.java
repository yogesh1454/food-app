package com.teadelivery.ordercatalog.common.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teadelivery.ordercatalog.common.dto.ImageProcessingResult;
import com.teadelivery.ordercatalog.common.service.ImageProcessingService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Consumes image processing results from Lambda via SQS
 * Updates database with CDN URLs and triggers search index sync
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageProcessingResultConsumer {

    private final ObjectMapper objectMapper;
    private final ImageProcessingService imageProcessingService;

    @SqsListener(value = "${aws.sqs.queues.image-processing-results}")
    public void processImageResult(String message) {
        try {
            log.info("Received image processing result message");

            ImageProcessingResult result = objectMapper.readValue(message, ImageProcessingResult.class);

            log.info("Processing image result: entity={}/{}, type={}, success={}",
                    result.getEntityType(), result.getEntityId(), result.getImageType(), result.isSuccess());

            if (!result.isSuccess()) {
                log.error("Image processing failed for {}/{}/{}",
                        result.getEntityType(), result.getEntityId(), result.getImageType());
                return;
            }

            // Update database based on entity type
            imageProcessingService.handleProcessingResult(result);

            log.info("Successfully processed image result for {}/{}/{}",
                    result.getEntityType(), result.getEntityId(), result.getImageType());

        } catch (Exception e) {
            log.error("Error processing image result message", e);
            // Throwing exception will cause message to be retried and eventually go to DLQ
            throw new RuntimeException("Failed to process image result", e);
        }
    }
}
