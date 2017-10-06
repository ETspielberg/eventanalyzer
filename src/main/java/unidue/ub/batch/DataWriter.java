package unidue.ub.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class DataWriter implements ItemWriter {

    private ObjectMapper mapper = new ObjectMapper();

    private String dataUrl;

    private String type;

    private static final Logger log = LoggerFactory.getLogger(DataWriter.class);

    public DataWriter setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
        return this;
    }

    public DataWriter setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public void write(List list) throws Exception {
        for (Object object : list) {
                String json = mapper.writeValueAsString(object);
                HttpClient client = new HttpClient();
                PostMethod post = new PostMethod(dataUrl + "/" + type);
                RequestEntity entity = new StringRequestEntity(json, "application/json", null);
                post.setRequestEntity(entity);
                int status = client.executeMethod(post);
                log.info("posted analysis with return status " + status);
        }
    }

}
