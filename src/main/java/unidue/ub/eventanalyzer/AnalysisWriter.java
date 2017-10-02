package unidue.ub.eventanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import unidue.ub.media.analysis.Eventanalysis;

import java.util.List;

public class AnalysisWriter implements ItemWriter {

    private ObjectMapper mapper = new ObjectMapper();

    private String dataUrl;

    private static final Logger log = LoggerFactory.getLogger(AnalysisWriter.class);

    public AnalysisWriter setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
        return this;
    }

    @Override
    public void write(List list) throws Exception {
        for (Object analysis : list) {
            if (((Eventanalysis) analysis).getStatus().equals("proposed")) {
                String json = mapper.writeValueAsString(analysis);
                HttpClient client = new HttpClient();
                PostMethod post = new PostMethod(dataUrl + "/eventanalysis");
                RequestEntity entity = new StringRequestEntity(json, "application/json", null);
                post.setRequestEntity(entity);
                int status = client.executeMethod(post);
                log.info("posted analysis with return status " + status);
            }
        }
    }

}
