package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Notation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Scope("Step")
public class ManifestationReader implements ItemReader<Manifestation> {

    private RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(ManifestationReader.class);

    private RestTemplate notationTemplate;

    private int nextManifestationIndex = 0;

    private List<Manifestation> manifestationData;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.getter.url}")
    private String getterURL;

    private Stockcontrol stockcontrol;

    ManifestationReader(Stockcontrol stockcontrol) {
        nextManifestationIndex = 0;
        this.stockcontrol = stockcontrol;
    }

    ManifestationReader setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        return this;
    }

    ManifestationReader setNotationTemplate(RestTemplate notationTemplate) {
        this.notationTemplate = notationTemplate;
        return this;
    }

    @Override
    public Manifestation read() throws Exception {
        if (noManifestationsFound()) {
            collectManifestation();
        }
        Manifestation nextManifestation = null;
        if (nextManifestationIndex < manifestationData.size()) {
            nextManifestation = manifestationData.get(nextManifestationIndex);
            nextManifestationIndex++;
        }
        return nextManifestation;
    }


    private void collectManifestation() throws URISyntaxException {
        List<Notation> notations = new ArrayList<>();
        String[] notationGroupStrings;
        if (stockcontrol.getSystemCode().isEmpty()) {
            Traverson traverson = new Traverson(new URI(settingsUrl + "/notation/search/getNotationListForNotationgroup?notationgroupName=" + stockcontrol.getSubjectID()), MediaTypes.HAL_JSON);
            Traverson.TraversalBuilder tb = traverson.follow("$._links.self.href");
            ParameterizedTypeReference<Resources<Notation>> typeRefDevices = new ParameterizedTypeReference<Resources<Notation>>() {};
            Resources<Notation> resUsers = tb.toObject(typeRefDevices);
            Collection<Notation> foundNotations = resUsers.getContent();
            for (Notation notationFound : foundNotations) {
                notations.add(notationFound);
                log.info("found notation " + notationFound.getNotation());
            }

        } else {
            if (stockcontrol.getSystemCode().contains(",")) {
                notationGroupStrings = stockcontrol.getSystemCode().split(",");
            } else {
                notationGroupStrings = new String[]{stockcontrol.getSystemCode()};
            }
            for (String notationGroupString : notationGroupStrings) {
                if (notationGroupString.contains("-")) {
                    String startNotation = notationGroupString.substring(0, notationGroupString.indexOf("-"));
                    String endNotation = notationGroupString.substring(notationGroupString.indexOf("-") + 1, notationGroupString.length());
                    Traverson traverson = new Traverson(new URI(settingsUrl + "/notation/search/getNotationList?startNotation=" + startNotation + "&endNotation=" + endNotation), MediaTypes.HAL_JSON);
                    Traverson.TraversalBuilder tb = traverson.follow("$._links.self.href");
                    ParameterizedTypeReference<Resources<Notation>> typeRefDevices = new ParameterizedTypeReference<Resources<Notation>>() {};
                    Resources<Notation> resUsers = tb.toObject(typeRefDevices);
                    Collection<Notation> foundNotations = resUsers.getContent();
                    for (Notation notationFound : foundNotations) {
                        notations.add(notationFound);
                        log.info("found notation " + notationFound.getNotation());
                    }
                } else {
                    ResponseEntity<Notation> response = notationTemplate.getForEntity(
                            settingsUrl + "/notation/" + notationGroupString,
                            Notation.class
                    );
                    notations.add(response.getBody());
                }
            }
        }
        manifestationData = new ArrayList<>();
        for (Notation notation : notations) {
            log.info("collecting manifestations for notation " + notation.getNotation());
            ResponseEntity<Manifestation[]> manifestations = restTemplate.getForEntity(
                    getterURL + "/getter/manifestations?identifier=" + notation.getNotation() + "&exact=&mode=notation",
                    Manifestation[].class
            );
            for (Manifestation manifestation : manifestations.getBody()) {
                ResponseEntity<Manifestation> fullManifestation = restTemplate.getForEntity(
                        getterURL + "/getter/buildFullManifestation?identifier=" + manifestation.getTitleID(),
                        Manifestation.class
                );
                manifestationData.add(fullManifestation.getBody());
            }
        }
    }

    private boolean noManifestationsFound() {
        return (this.manifestationData == null);
    }

    List<Manifestation> getManifestations() { return manifestationData; }
}
