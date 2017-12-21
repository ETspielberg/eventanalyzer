package unidue.ub.batch.eventanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import unidue.ub.settings.fachref.Status;
import unidue.ub.settings.fachref.Stockcontrol;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class StockcontrolSettingTasklet implements Tasklet {

    private String settingsUrl;

    private Status status;

    private final static Logger log = LoggerFactory.getLogger(StockcontrolSettingTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws IOException {
        Stockcontrol stockcontrol = (Stockcontrol) chunkContext.getStepContext().getJobExecutionContext().get("stockcontrol");
        stockcontrol.setStatus(status);
        stockcontrol.setLastrun(Timestamp.valueOf(LocalDateTime.now()));
        String json = new ObjectMapper().writeValueAsString(stockcontrol);
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(settingsUrl + "/stockcontrol");
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        int responseStatus = client.executeMethod(post);
        log.info("set stockcontrol '" + stockcontrol.getIdentifier() + "' status to " + status + " with return status " + responseStatus);
        return RepeatStatus.FINISHED;
    }

    StockcontrolSettingTasklet setSettingsUrl(String settingsUrl) {
        this.settingsUrl = settingsUrl;
        return this;
    }

    StockcontrolSettingTasklet setStatus(Status status) {
        this.status = status;
        return this;
    }
}
