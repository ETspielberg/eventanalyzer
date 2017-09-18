package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.*;

public class ManifestationProcessor implements ItemProcessor<Manifestation,Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ManifestationProcessor.class);

    private Stockcontrol stockcontrol;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    ManifestationProcessor(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
    }

    @Override
    public Eventanalysis process(final Manifestation manifestation) throws Exception {
        log.info("analyzing manifestation " + manifestation.getTitleID() + " and shelfmark " + manifestation.getShelfmark());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(),stockcontrol.getMaterials());
        for (Item item : manifestation.getItems()) {
            if (itemFilter.matches(item)) {
                List<Event> itemEvents = item.getEvents();
                for (Event event : itemEvents) {
                    events.add(event);
                    if (event.getEndEvent() != null)
                        events.add(event.getEndEvent());
                }
            }
        }
        Eventanalysis analysis = new EventAnalyzer(settingsUrl).analyze(events, stockcontrol);
        analysis.setTitleId(manifestation.getTitleID());
        analysis.setShelfmark(manifestation.getShelfmark());
        analysis.setMab(manifestation.getBibliographicInformation().toString());
        return analysis;
    }
}
