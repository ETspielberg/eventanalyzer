package unidue.ub.eventanalyzer;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Notation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManifestationReader implements ItemReader<Manifestation> {

    private RestTemplate restTemplate;

    private RestTemplate notationTemplate;

    private Stockcontrol stockcontrol;

    private int nextManifestationIndex = 0;

    private List<Manifestation> manifestationData;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.getter.url}")
    private String getterURL;

    ManifestationReader(Stockcontrol stockcontrol, RestTemplate restTemplate) {
        this.stockcontrol = stockcontrol;
        this.restTemplate = restTemplate;
        nextManifestationIndex = 0;
    }

    ManifestationReader() {
        nextManifestationIndex = 0;
    }

    public ManifestationReader setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        return this;
    }

    public ManifestationReader setNotationTemplate(RestTemplate notationTemplate) {
        this.notationTemplate = notationTemplate;
        return this;
    }

    public ManifestationReader setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
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

    private void collectManifestation() {
        List<Notation> notations = new ArrayList<>();
        String[] notationGroupStrings;
        if (stockcontrol.getSystemCode().contains(",")) {
            notationGroupStrings = stockcontrol.getSystemCode().split(",");
        } else {
            notationGroupStrings = new String[] {stockcontrol.getSystemCode()};
        }
        for (String notationGroupString : notationGroupStrings) {
            if (notationGroupString.contains("-")) {
                String startNotation = notationGroupString.substring(0,notationGroupString.indexOf("-"));
                String endNotation = notationGroupString.substring(notationGroupString.indexOf("-") + 1, notationGroupString.length());
                ResponseEntity<Notation[]> response = notationTemplate.getForEntity(
                        settingsUrl + "/notation/search/getNotationList?startNotation=" + startNotation + "&endNotation=" + endNotation,
                        Notation[].class
                );
                notations.addAll(Arrays.asList(response.getBody()));
            } else {
                ResponseEntity<Notation[]> response = notationTemplate.getForEntity(
                        settingsUrl + "/notation/search/getNotationListForNotationgroup?notationGroupName=" + notationGroupString,
                        Notation[].class
                );
                notations.addAll(Arrays.asList(response.getBody()));
            }
        }
        manifestationData = new ArrayList<>();
        for (Notation notation : notations) {
            ResponseEntity<Manifestation[]> manifestations = restTemplate.getForEntity(
                    getterURL + "/manifestations?identifier=" + notation.getNotation() + "&mode=notation",
                    Manifestation[].class
            );
            manifestationData.addAll(Arrays.asList(manifestations.getBody()));
        }
    }

    private boolean noManifestationsFound() {
        return (this.manifestationData == null);
    }
}
