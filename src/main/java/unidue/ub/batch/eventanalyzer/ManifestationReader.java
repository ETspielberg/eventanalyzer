package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Notation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManifestationReader implements ItemReader<Manifestation> {

    private static final Logger log = LoggerFactory.getLogger(ManifestationReader.class);

    private int nextManifestationIndex;

    private List<Manifestation> manifestationData;

    private Stockcontrol stockcontrol;

    public ManifestationReader() {
        nextManifestationIndex = 0;
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


    public void collectManifestation() throws URISyntaxException {
        List<String> notations = new ArrayList<>();
        String[] notationGroupStrings;
        if (stockcontrol.getSystemCode().isEmpty()) {
            Traverson traverson = new Traverson(new URI("http://localhost:8082/api/settings/notation/search/getNotationListForNotationgroup?notationgroupName=" + stockcontrol.getSubjectID()), MediaTypes.HAL_JSON);
            Traverson.TraversalBuilder tb = traverson.follow("$._links.self.href");
            ParameterizedTypeReference<Resources<Notation>> typeRefDevices = new ParameterizedTypeReference<Resources<Notation>>() {
            };
            Resources<Notation> resUsers = tb.toObject(typeRefDevices);
            Collection<Notation> foundNotations = resUsers.getContent();
            for (Notation notationFound : foundNotations) {
                notations.add(notationFound.getNotation());
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
                    Traverson traverson = new Traverson(new URI("http://localhost:8082/api/settings/notation/search/getNotationList?startNotation=" + startNotation + "&endNotation=" + endNotation), MediaTypes.HAL_JSON);
                    Traverson.TraversalBuilder tb = traverson.follow("$._links.self.href");
                    ParameterizedTypeReference<Resources<Notation>> typeRefDevices = new ParameterizedTypeReference<Resources<Notation>>() {
                    };
                    Resources<Notation> resUsers = tb.toObject(typeRefDevices);
                    Collection<Notation> foundNotations = resUsers.getContent();
                    for (Notation notationFound : foundNotations) {
                        notations.add(notationFound.getNotation());
                        log.info("found notation " + notationFound.getNotation());
                    }
                } else {
                    notations.add(notationGroupString);
                }
            }
        }
        manifestationData = new ArrayList<>();
        for (String notation : notations) {
            try {
                ResponseEntity<Manifestation[]> manifestations = new RestTemplate().getForEntity(
                        "http://localhost:8082/getter/manifestations?identifier=" + notation + "&exact=&mode=notation",
                        Manifestation[].class
                );
                log.info("queried notation " + notation);
            for (Manifestation manifestation : manifestations.getBody()) {
                String titleID = manifestation.getTitleID();
                log.info("querying manifestation " + titleID);
                if (titleID != null) {
                    try {
                        ResponseEntity<Manifestation> fullManifestation = new RestTemplate().getForEntity(
                                "http://localhost:8082/getter/buildFullManifestation?identifier=" + manifestation.getTitleID(),
                                Manifestation.class
                        );
                        manifestationData.add(fullManifestation.getBody());
                    } catch (ResourceAccessException rea) {
                        log.warn("could not retrieve full manifestation for " + manifestation.getTitleID());
                    }
                }
            }
            } catch (Exception e) {
                log.warn("could not get manifestations for " + notation);
                log.error(e.getMessage());
            }
        }
    }


    public void setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
    }

    private boolean noManifestationsFound() {
        return (this.manifestationData == null);
    }

    List<Manifestation> getManifestations() {
        return manifestationData;
    }

    @BeforeStep
    public void retrieveStockcontrol(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.stockcontrol = (Stockcontrol) jobContext.get("stockcontrol");
        log.info("retrieved stockcontrol " + stockcontrol.toString() + " from execution context by manifestation reader");
    }
}
