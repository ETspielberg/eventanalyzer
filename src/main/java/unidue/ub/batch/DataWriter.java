package unidue.ub.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Map;

public class DataWriter implements ItemWriter {

    private static final Logger log = LoggerFactory.getLogger(DataWriter.class);
    private ObjectMapper mapper = new ObjectMapper();
    private static final HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();

    @Override
    public void write(List list) throws Exception {
        long successful = 0;
        for (Object object : list) {
            String json = mapper.writeValueAsString(object);
            HttpClient client = new HttpClient(httpConnectionManager);
            String objectType = object.getClass().getSimpleName();
            PostMethod post = new PostMethod("http://localhost:8082/api/data/" + objectType.toLowerCase());
            RequestEntity entity = new StringRequestEntity(json, "application/json", null);
            post.setRequestEntity(entity);
            int status = client.executeMethod(post);
            if (status == 201)
                successful++;
            //else log.info("posted " + objectType + " to api/data/" + objectType.toLowerCase() + " with return status " + status);
            post.releaseConnection();
        }
        log.info("successfully posted " + successful + " of " + list.size() + " counterbuilder data.");
    }

}
