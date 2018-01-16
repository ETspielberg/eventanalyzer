package unidue.ub.batch.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Manifestation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NrequestsReader implements ItemReader<Manifestation> {

    private List<Manifestation> manifestations = new ArrayList<>();

    private boolean collected = false;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Manifestation read() throws Exception {
        if (!collected)
            collectManifestationsByOpenRequests();
        if (manifestations.size() > 0)
            return manifestations.remove(0);
        return null;
    }

    private void collectManifestationsByOpenRequests() throws IOException {
        collected = true;
        ResponseEntity<Manifestation[]> response = new RestTemplate().getForEntity(
                "/getter/manifestations?identifier=&exact=&mode=openRequests",
                Manifestation[].class
        );
        log.info("found " + response.getBody().length + " manifestations with open requests");

        Manifestation[] foundManifestations = response.getBody();
        int totalNumber = foundManifestations.length;
        for (int i = 0; i < totalNumber; i++) {
            Manifestation manifestation = foundManifestations[i];
            double fraction = 100 * (double) i / (double) totalNumber;
            log.info("retrieving details for manifestation " + i + " (" + manifestation.getTitleID() + ") of " + totalNumber + "(" + fraction + " %)");
            ResponseEntity<Manifestation> responseInd = new RestTemplate().getForEntity(
                    "/getter/buildActiveManifestation?identifier=" + manifestation.getTitleID(),
                    Manifestation.class
            );
            manifestations.add(responseInd.getBody());
        }
    }
}
