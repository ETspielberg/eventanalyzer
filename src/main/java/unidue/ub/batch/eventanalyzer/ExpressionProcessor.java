package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Item;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.ArrayList;
import java.util.List;

@StepScope
public class ExpressionProcessor implements ItemProcessor<Expression,Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ExpressionProcessor.class);

    private Stockcontrol stockcontrol;

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    public ExpressionProcessor() {
    }

    @Override
    public Eventanalysis process(final Expression expression) throws Exception {
        log.info("analyzing expression  " + expression.getShelfmarkBase() + " and shelfmark " + expression.getShelfmarkBase());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(),stockcontrol.getMaterials());
        for (Item item : expression.getItems()) {
            if (itemFilter.matches(item)) {
                List<Event> itemEvents = item.getEvents();
                for (Event event : itemEvents) {
                    events.add(event);
                    if (event.getEndEvent() != null)
                        events.add(event.getEndEvent());
                }
            }
        }
        Eventanalysis analysis = new EventAnalyzer(settingsUrl).analyze(events,stockcontrol);
        if (analysis.getProposedPurchase() > 0 || analysis.getProposedDeletion() > 0) {
            analysis.setTitleId(expression.getShelfmarkBase());
            analysis.setShelfmark(expression.getShelfmarkBase());
            analysis.setMab(expression.getBibliographicInformation().toString());
            return analysis;
        } else
            return null;
    }

    @BeforeStep
    public void retrieveStockcontrol(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.stockcontrol = (Stockcontrol) jobContext.get("stockcontrol");
        log.info("retrieved stockcontrol " + stockcontrol.toString() + " from execution context by expression processor" );
    }
}
