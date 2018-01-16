package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Item;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.ArrayList;
import java.util.List;

public class ManifestationProcessor implements ItemProcessor<Manifestation, Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ManifestationProcessor.class);

    private Stockcontrol stockcontrol;

    public ManifestationProcessor() {
    }

    @Override
    public Eventanalysis process(final Manifestation manifestation) throws Exception {
        log.info("analyzing manifestation " + manifestation.getTitleID() + " and shelfmark " + manifestation.getShelfmark());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(), stockcontrol.getMaterials());
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
        Eventanalysis analysis = new EventAnalyzer().analyze(events, stockcontrol);
        if (analysis.getProposedDeletion() > 0 || analysis.getProposedPurchase() > 0) {
            analysis.setTitleId(manifestation.getTitleID());
            analysis.setShelfmark(manifestation.getShelfmark());
            analysis.setMab(manifestation.getBibliographicInformation().toString());
            return analysis;
        } else
            return null;
    }

    @BeforeStep
    public void retrieveStockcontrol(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.stockcontrol = (Stockcontrol) jobContext.get("stockcontrol");
        log.info("retrieved stockcontrol " + stockcontrol.toString() + " from execution context by manifestation reader");
    }
}
