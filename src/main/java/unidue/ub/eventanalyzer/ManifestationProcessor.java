package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;

import java.util.*;

@StepScope
public class ManifestationProcessor implements ItemProcessor<Manifestation,Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ManifestationProcessor.class);

    @Value("#{jobParameters['collections']}")
    private String collections;

    @Value("#{jobParameters['materials']}")
    private String materials;

    @Override
    public Eventanalysis process(final Manifestation manifestation) throws Exception {
        log.info("analyzing manifestation " + manifestation.getTitleID() + " and shelfmark " + manifestation.getShelfmark());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(collections,materials);
        for (Item item : manifestation.getItems()) {
            if (itemFilter.matches(item))
                events.addAll(item.getEvents());
        }
        EventAnalyzer analyzer = new EventAnalyzer(events,manifestation.getTitleID());
        return analyzer.getEventanalysis();
    }
}
