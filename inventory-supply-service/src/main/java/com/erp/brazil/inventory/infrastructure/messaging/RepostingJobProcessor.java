package com.erp.brazil.inventory.infrastructure.messaging;

import com.erp.brazil.inventory.application.usecase.RecalculateValuationUseCase;
import com.erp.brazil.inventory.domain.model.RepostingJob;
import com.erp.brazil.inventory.domain.repository.RepostingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepostingJobProcessor {

    private final RepostingJobRepository jobRepository;
    private final RecalculateValuationUseCase recalculateValuationUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue.reposting:inventory.reposting.queue}")
    public void processRepostingJob(Map<String, Object> message) {
        Object jobIdObj = message.get("jobId");
        if (jobIdObj == null) {
            log.warn("Received reposting message without jobId");
            return;
        }

        UUID jobId = UUID.fromString(jobIdObj.toString());
        log.info("Processing reposting job: {}", jobId);

        try {
            recalculateValuationUseCase.recalculateValuation(UUID.randomUUID(), jobId); // companyId would come from message
        } catch (Exception e) {
            log.error("Failed to process reposting job: {}", jobId, e);
            throw e; // Re-throw for retry
        }
    }
}