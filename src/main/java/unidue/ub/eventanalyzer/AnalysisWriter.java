package unidue.ub.eventanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.monographs.Manifestation;

import java.util.List;

public class AnalysisWriter<Eventanalysis> implements ItemWriter {

    @Value("${ub.statistics.data.url}")
    private String dataURL;

    private ObjectMapper mapper = new ObjectMapper();


    @Override
    public void write(List list) throws Exception {
        String json = mapper.writeValueAsString(list);
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(dataURL + "/eventanalysis");
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        client.executeMethod(post);
    }
}
