package unidue.ub.eventanalyzer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Notation;

import javax.batch.api.chunk.ItemReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManifestationReader implements ItemReader{

    private boolean noInput;

    private String notationQuery;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.getter.url}")
    private String getterURL;

    private ObjectMapper mapper = new ObjectMapper();

    private Iterator<Manifestation> iterator;

    @Override
    public void open(Serializable serializable) throws Exception {
        Assert.notNull(notationQuery, "Input notations must be set");
        noInput = true;

        List<Notation> notations;
        String notationsAsJson = getObject(settingsUrl + "/notation/search/findByNotationRange?notationRange=" + notationQuery);
        notations = mapper.readValue(notationsAsJson, new TypeReference<List<Notation>>(){});
        List<Manifestation> manifestations= new ArrayList<>();
        for (Notation notation : notations) {
            String manifestationsAsJSON = getObject(getterURL + "/manifestations?identifier=" + notation + "&mode=notation");
            List<Manifestation> manifestationsInd = mapper.readValue(manifestationsAsJSON, new TypeReference<List<Manifestation>>() {
            });
            manifestations.addAll(manifestationsInd);
        }
        if (manifestations.size() > 0)
            noInput = false;
        iterator = manifestations.iterator();
    }

    @Override
    public void close() {

    }

    @Override
    public Manifestation readItem() throws Exception {
        return iterator.next();
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }

    private String getObject(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        return get.getResponseBodyAsString();
    }

    public void setNotationQuery(String notationQuery) {
        this.notationQuery = notationQuery;
    }

    public boolean isNoInput() {
        return noInput;
    }
}
