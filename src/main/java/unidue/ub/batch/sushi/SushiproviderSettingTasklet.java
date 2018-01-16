package unidue.ub.batch.sushi;

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
import unidue.ub.settings.fachref.Sushiprovider;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SushiproviderSettingTasklet implements Tasklet {

    private final static Logger log = LoggerFactory.getLogger(SushiproviderSettingTasklet.class);
    private Status status;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws IOException {
        Sushiprovider sushiprovider = (Sushiprovider) chunkContext.getStepContext().getJobExecutionContext().get("sushiprovider");
        sushiprovider.setStatus(status);
        sushiprovider.setLastrun(Timestamp.valueOf(LocalDateTime.now()));
        String json = new ObjectMapper().writeValueAsString(sushiprovider);
        log.info(json);
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("/api/settings/sushiprovider");
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        int responseStatus = client.executeMethod(post);
        log.info("set sushiprovider '" + sushiprovider.getIdentifier() + "' status to " + status + " with return status " + responseStatus);
        return RepeatStatus.FINISHED;
    }

    SushiproviderSettingTasklet setStatus(Status status) {
        this.status = status;
        return this;
    }
}
