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

    private static final Logger log = LoggerFactory.getLogger(DataWriter.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void write(List list) throws Exception {
        for (Object object : list) {
            String json = mapper.writeValueAsString(object);
            HttpClient client = new HttpClient();
            String objectType = object.getClass().getSimpleName();
            PostMethod post = new PostMethod("/api/data/" + objectType.toLowerCase());
            RequestEntity entity = new StringRequestEntity(json, "application/json", null);
            post.setRequestEntity(entity);
            int status = client.executeMethod(post);
            log.info("posted " + objectType + " to api/data/" + objectType.toLowerCase() + " with return status " + status);
        }
    }

}
