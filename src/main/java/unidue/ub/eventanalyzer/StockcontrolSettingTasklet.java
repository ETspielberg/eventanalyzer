package unidue.ub.eventanalyzer;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.IOException;

public class StockcontrolSettingTasklet implements Tasklet {

    private String settingsUrl;

    private Stockcontrol stockcontrol;

    private String status;

    private final static Logger log = LoggerFactory.getLogger(StockcontrolSettingTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws IOException {
        stockcontrol.setStatus(status);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(stockcontrol);
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(settingsUrl + "/stockcontrol");
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        int responseStatus = client.executeMethod(post);
        log.info("set stockcontrol" + stockcontrol.getIdentifier() + "status to " + status + " with return status " + responseStatus);
        return RepeatStatus.FINISHED;
    }

    public String getSettingsUrl() {
        return settingsUrl;
    }

    public StockcontrolSettingTasklet setSettingsUrl(String settingsUrl) {
        this.settingsUrl = settingsUrl;
        return this;
    }

    public Stockcontrol getStockcontrol() {
        return stockcontrol;
    }

    public StockcontrolSettingTasklet setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public StockcontrolSettingTasklet setStatus(String status) {
        this.status = status;
        return this;
    }
}
