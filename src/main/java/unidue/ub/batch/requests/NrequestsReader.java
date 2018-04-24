package unidue.ub.batch.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Manifestation;

import java.util.ArrayList;
import java.util.List;

public class NrequestsReader implements ItemReader<Manifestation> {

    private List<Manifestation> manifestations = new ArrayList<>();

    private boolean collected = false;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Manifestation read() {
        if (!collected)
            collectManifestationsByOpenRequests();
        if (manifestations.size() > 0)
            return manifestations.remove(0);
        return null;
    }

    private void collectManifestationsByOpenRequests() {
        collected = true;
        ResponseEntity<Manifestation[]> response = new RestTemplate().getForEntity(
                "http://localhost:8082/getter/manifestations?identifier=&exact=&mode=openRequests",
                Manifestation[].class
        );
        log.info("found " + response.getBody().length + " manifestations with open requests");

        for (Manifestation manifestation : response.getBody()) {
            try {
                ResponseEntity<Manifestation> responseInd = new RestTemplate().getForEntity(
                        "http://localhost:8082/getter/buildActiveManifestation?identifier=" + manifestation.getTitleID(),
                        Manifestation.class
                );
                manifestations.add(responseInd.getBody());
            } catch (Exception e ) {
                log.warn("could not read manifestation " + manifestation.getTitleID());
            }
        }
    }
}
