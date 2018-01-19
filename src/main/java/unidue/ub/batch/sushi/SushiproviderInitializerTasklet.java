package unidue.ub.batch.sushi;

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
import unidue.ub.settings.fachref.Status;
import unidue.ub.settings.fachref.Sushiprovider;

@Component
@StepScope
public class SushiproviderInitializerTasklet implements Tasklet {

    @Value("#{jobParameters['sushiprovider.identifier'] ?: 'newProvider'}")
    public String identifier;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws Exception {
        Sushiprovider sushiprovider = getSushiprovider();
        ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        stepContext.put("sushiprovider", sushiprovider);
        log.info("stored sushiprovider " + identifier + " in job context");
        return RepeatStatus.FINISHED;
    }

    private Sushiprovider getSushiprovider() {
        log.info("retrieving sushiprovider with identifier " + identifier + " from " + "/api/settings/sushiprovider/" + identifier);
        Sushiprovider sushiprovider = new Sushiprovider();
        sushiprovider.setName("newProvider");
        try {
            ResponseEntity<Sushiprovider> response = new RestTemplate().getForEntity(
                    "http://localhost:8082/api/settings/sushiprovider/" + identifier,
                    Sushiprovider.class,
                    0);
            if (response.getStatusCodeValue() == 200)
                sushiprovider = response.getBody();
        } catch (Exception e) {
            log.info("could not retrieve sushi provider");
        }
        return sushiprovider;
    }

}
