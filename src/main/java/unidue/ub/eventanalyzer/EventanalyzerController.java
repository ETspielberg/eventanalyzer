package unidue.ub.eventanalyzer;

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
import unidue.ub.settings.fachref.FachrefProcess;
import unidue.ub.settings.fachref.Notation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.io.IOException;
import java.util.List;

/**
 * Created by Eike on 05.07.2017.
 */
@Controller
@RequestMapping("/batch")
public class EventanalyzerController {

    @Value("${ub.statistics.settings.url}")
    private  String settingsUrl;

    @RequestMapping("/eventanalyzer")
    public ResponseEntity<?> run(@RequestParam("identifier") String identifier) {
        FachrefProcess process = new FachrefProcess();
        process.setStatus("started");
        Stockcontrol stockcontrol = new Stockcontrol();
        try {
            stockcontrol = getStockcontrol(identifier);
        } catch (IOException e) {
            process.setStatus("error");
            process.setMessage("could not load stockcontrol");
            return ResponseEntity.ok(process);
        }
        String notationsAsJson = "";
        if (stockcontrol.getSystemCode().isEmpty()) {
            try {
                notationsAsJson = getObject(settingsUrl + "/notation/search/findByNotationRange?notationRange=" + stockcontrol.getSystemCode());
            } catch (IOException e) {
                process.setStatus("error");
                process.setMessage("could not retrieve notations");
                return ResponseEntity.ok(process);
            }
    }  else {
            try {
                notationsAsJson = getObject(settingsUrl + "/notation/search/getNotationListForNotationgroup?notationGroup=" + stockcontrol.getSubjectID());
            } catch (IOException e) {
                process.setStatus("error");
                process.setMessage("could not retrieve notations");
                return ResponseEntity.ok(process);
            }
        }
        List<Notation> notations =





    }

    private Stockcontrol getStockcontrol(String identifier) throws IOException {
        String json = getObject(settingsUrl + "/stockcontrol/" + identifier);
        ObjectMapper mapper = new ObjectMapper();
        Stockcontrol stockcontrol = mapper.readValue(json, Stockcontrol.class);
        return stockcontrol;
    }

    private List<Notation> getNotationList(String notationRange) throws IOException {
        String json = getObject(settingsUrl + "/stockcontrol/" + identifier);
        ObjectMapper mapper = new ObjectMapper();
        Stockcontrol stockcontrol = mapper.readValue(json, Stockcontrol.class);
        return stockcontrol;
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
    }
}
