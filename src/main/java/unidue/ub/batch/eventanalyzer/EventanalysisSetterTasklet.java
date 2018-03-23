package unidue.ub.batch.eventanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import unidue.ub.settings.fachref.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@StepScope
public class EventanalysisSetterTasklet implements Tasklet {

    private Status status;

    @Value("#{jobParameters['stockcontrol.identifier'] ?: 'newProfile'}")
    public String identifier;

    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws IOException {
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod("http://localhost:8082/api/data/eventanalysis/setAnalysisToOld");
        Map<String,String> parameters = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        parameters.put("identifier",identifier);
        parameters.put("status","OBSOLETE");
        String json = mapper.writeValueAsString(parameters);
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        postMethod.setRequestEntity(entity);
        int status = client.executeMethod(postMethod);
        return RepeatStatus.FINISHED;
    }

    EventanalysisSetterTasklet setStatus(Status status) {
        this.status = status;
        return this;
    }
}
