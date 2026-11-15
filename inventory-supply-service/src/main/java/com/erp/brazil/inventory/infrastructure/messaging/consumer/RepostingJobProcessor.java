package com.erp.brazil.inventory.infrastructure.messaging.consumer;

import com.erp.brazil.inventory.application.usecase.RecalculateValuationUseCase;
import com.erp.brazil.inventory.infrastructure.messaging.event.RepostingTriggerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RepostingJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(RepostingJobProcessor.class);

    private final RecalculateValuationUseCase recalculateValuationUseCase;

    public RepostingJobProcessor(RecalculateValuationUseCase recalculateValuationUseCase) {
        this.recalculateValuationUseCase = recalculateValuationUseCase;
    }

    @RabbitListener(queues = "${app.messaging.queue.reposting:inventory.reposting.trigger}")
    public void handleRepostingTrigger(RepostingTriggerEvent event) {
        log.info("Received reposting trigger event: jobId={}, companyId={}, itemId={}, warehouseId={}",
                event.getJobId(), event.getCompanyId(), event.getItemId(), event.getWarehouseId());

        try {
            RecalculateValuationUseCase.Input input = new RecalculateValuationUseCase.Input(event.getJobId());
            RecalculateValuationUseCase.Output output = recalculateValuationUseCase.execute(input);

            log.info("Reposting job completed: jobId={}, processed={}, failed={}, status={}",
                    output.job().getId(), output.processedCount(), output.failedCount(), output.job().getStatus());

        } catch (Exception e) {
            log.error("Error processing reposting job: {}", event.getJobId(), e);
        }
    }
}