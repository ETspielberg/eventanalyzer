package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.*;

@Scope(value = "step")
public class ManifestationProcessor implements ItemProcessor<Manifestation,Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ManifestationProcessor.class);

    private Stockcontrol stockcontrol;

    @Override
    public Eventanalysis process(final Manifestation manifestation) throws Exception {
        log.info("analyzing manifestation " + manifestation.getTitleID() + " and shelfmark " + manifestation.getShelfmark());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(),stockcontrol.getMaterials());
        for (Item item : manifestation.getItems()) {
            if (itemFilter.matches(item))
                events.addAll(item.getEvents());
        }
        EventAnalyzer analyzer = new EventAnalyzer(events,manifestation.getTitleID(),stockcontrol);
        return analyzer.getEventanalysis();
    }

    public ManifestationProcessor setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
        return this;
    }
}
