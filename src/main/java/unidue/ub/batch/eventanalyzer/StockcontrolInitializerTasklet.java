package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import unidue.ub.settings.fachref.Stockcontrol;

@Component
@StepScope
public class StockcontrolInitializerTasklet implements Tasklet {

    @Value("#{jobParameters['stockcontrol.identifier'] ?: 'newProfile'}")
    public String identifier;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {
        Stockcontrol stockcontrol = getStockcontrol();
        ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        stepContext.put("stockcontrol", stockcontrol);
        log.info("stored stockcontrol " + identifier + " in job context");
        return RepeatStatus.FINISHED;
    }

    private Stockcontrol getStockcontrol() {
        log.info("retrieving stockcontrol for identifier " + identifier);
        ResponseEntity<Stockcontrol> response = new RestTemplate().getForEntity(
                "http://localhost:8082/api/settings/stockcontrol/" + identifier,
                Stockcontrol.class,
                0);
        Stockcontrol stockcontrol = new Stockcontrol();
        stockcontrol.setGroupedAnalysis(false);
        if (response.getStatusCodeValue() == 200)
            stockcontrol = response.getBody();
        return stockcontrol;
    }

}
