package unidue.ub.batch.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.analysis.Nrequests;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.ItemGroup;

import java.net.URL;

import static unidue.ub.media.tools.MonographTools.getNrequestsFor;

public class NrequestsProcessor implements ItemProcessor<Manifestation,Nrequests> {

    public NrequestsProcessor() { }

    private RestTemplate restTemplate;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Nrequests process(final Manifestation manifestation) throws Exception {
            log.info("transforming manifestation with id " + manifestation.getTitleID());
            ObjectMapper mapper = new ObjectMapper();
            String lendable = mapper.readValue(new URL(settingsUrl + "/itemGroup/lendable"), ItemGroup.class).getItemCategoriesAsString();
            return getNrequestsFor(manifestation, lendable);
    }
}
