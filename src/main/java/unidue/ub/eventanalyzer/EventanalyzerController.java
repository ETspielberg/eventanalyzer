package unidue.ub.eventanalyzer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.FachrefProcess;
import unidue.ub.settings.fachref.Notation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Eike on 05.07.2017.
 */
@Controller
@RequestMapping("/batch")
public class EventanalyzerController {

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.monitor.url}")
    private String monitorURL;

    @Value("${ub.statistics.getter.url}")
    private String getterURL;

    @Value("${ub.statistics.data.url}")
    private String dataURL;

    private FachrefProcess process;

    private ObjectMapper mapper = new ObjectMapper();

    private Stockcontrol stockcontrol;

    private List<Manifestation> manifestations;



    @RequestMapping("/eventanalyzer")
    public ResponseEntity<?> run(@RequestParam("identifier") String identifier) {
        process = new FachrefProcess();
        process.setDate(new Date());
        process.setIdentifier(identifier);
        process.setStatus("started");

        try {
            saveObject(mapper.writeValueAsString(process),monitorURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String json = getObject(settingsUrl + "/stockcontrol/" + identifier);
            stockcontrol = mapper.readValue(json, Stockcontrol.class);
        } catch (IOException e) {
            process.setStatus("error");
            process.setMessage("could not load stockcontrol");
            return ResponseEntity.ok(process);
        }

        String notationsAsJson;
        List<Notation> notations;
        try {
            if (stockcontrol.getSystemCode().isEmpty()) {
                notationsAsJson = getObject(settingsUrl + "/notation/search/findByNotationRange?notationRange=" + stockcontrol.getSubjectID());
            } else {
                notationsAsJson = getObject(settingsUrl + "/notation/search/getNotationListForNotationgroup?notationGroup=" + stockcontrol.getSystemCode());
            }
            notations = mapper.readValue(notationsAsJson, new TypeReference<List<Notation>>(){});
        } catch (IOException e) {
            process.setStatus("error");
            process.setMessage("could not retrieve notations");
            return ResponseEntity.ok(process);
        }
        try {
            process.setStatus("running");
            updateObject(mapper.writeValueAsString(process),monitorURL,process.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        manifestations= new ArrayList<>();
        for (Notation notation : notations) {
            try {
                String manifestationsAsJSON = getObject(getterURL +"/manifestations?identifier="+ notation + "&mode=notation");
                List<Manifestation> manifestationsInd = mapper.readValue(manifestationsAsJSON,new TypeReference<List<Manifestation>>(){});
                manifestations.addAll(manifestationsInd);
            } catch (IOException e) {
                process.setStatus("warning");
                process.addMessage("could not retrieve manifestations for notation " + notation);
            }
        }

        analyzeManifestations();

        process.setStatus("finished");

    return ResponseEntity.ok(process);
    }

    private void analyzeManifestations() {
        List<Eventanalysis> analyses = new ArrayList<>();
        for (Manifestation manifestation : manifestations) {
            try {
                String manifestationsAsJSON = getObject(getterURL +"fullManifestation?identifier="+ manifestation.getTitleID() + "/exact=true");
                Manifestation manifestationInd = mapper.readValue(manifestationsAsJSON, Manifestation.class);
                ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(),stockcontrol.getMaterials());
                List<Event> events = new ArrayList<>();
                for (Item item : manifestationInd.getItems()) {
                    if (itemFilter.matches(item))
                        events.addAll(item.getEvents());
                }
                EventAnalyzer anaylzer = new EventAnalyzer(events, manifestationInd.getTitleID(), stockcontrol);
                analyses.add(anaylzer.getEventanalysis());
            } catch (IOException ie) {
                process.setStatus("warning");
                process.addMessage("could not retrieve manifestations for notation " + manifestation.getTitleID() + "with shelfamrk " + manifestation.getShelfmark());
            }
        }
        try {
            String analysesAsJSON = mapper.writeValueAsString(analyses);
            saveObject(analysesAsJSON,dataURL);
        } catch (IOException ie2) {
            process.setStatus("error");
            process.addMessage("could not save analyses");
        }

    }

    private String getObject(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        return get.getResponseBodyAsString();
    }

    private void saveObject(String json, String url) throws IOException {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        client.executeMethod(post);
        String response = post.getResponseBodyAsString();
        FachrefProcess savedProcess = mapper.readValue(response,FachrefProcess.class);
        process.setId(savedProcess.getId());

    }

    private void updateObject(String json, String url, long id) throws IOException {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url + "/" + id);
        RequestEntity entity = new StringRequestEntity(json, "application/json", null);
        post.setRequestEntity(entity);
        client.executeMethod(post);
    }
}
