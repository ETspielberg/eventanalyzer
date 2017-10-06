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
import unidue.ub.settings.fachref.Stockcontrol;
import unidue.ub.settings.fachref.Sushiprovider;

import java.io.IOException;
import java.util.Date;

public class SushiproviderSettingTasklet implements Tasklet {

    private String settingsUrl;

    private String status;

    private final static Logger log = LoggerFactory.getLogger(SushiproviderSettingTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws IOException {
        Sushiprovider sushiprovider = (Sushiprovider) chunkContext.getStepContext().getJobExecutionContext().get("sushiprovider");
        sushiprovider.setStatus(status);
        sushiprovider.setLastRun(new Date());
        String json = new ObjectMapper().writeValueAsString(sushiprovider);
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(settingsUrl + "/stockcontrol");
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        int responseStatus = client.executeMethod(post);
        log.info("set sushiprovider '" + sushiprovider.getId() + "' status to " + status + " with return status " + responseStatus);
        return RepeatStatus.FINISHED;
    }

    SushiproviderSettingTasklet setSettingsUrl(String settingsUrl) {
        this.settingsUrl = settingsUrl;
        return this;
    }

    SushiproviderSettingTasklet setStatus(String status) {
        this.status = status;
        return this;
    }
}
