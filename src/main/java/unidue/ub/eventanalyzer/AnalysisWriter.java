package unidue.ub.eventanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.analysis.Eventanalysis;

import java.util.List;

public class AnalysisWriter implements ItemWriter {

    @Value("${ub.statistics.data.url}")
    private String dataURL;

    private ObjectMapper mapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(AnalysisWriter.class);

    @Override
    public void write(List list) throws Exception {
        for (Object analysis  :list) {
            if (((Eventanalysis) analysis).getStatus().equals("finished")) {
                String json = mapper.writeValueAsString(analysis);
                HttpClient client = new HttpClient();
                PostMethod post = new PostMethod(dataURL + "/eventanalysis");
                RequestEntity entity = new StringRequestEntity(json, "application/json", null);
                post.setRequestEntity(entity);
                int status = client.executeMethod(post);
                log.info("posted analysis with return status " + status);
            }
        }
    }
}
