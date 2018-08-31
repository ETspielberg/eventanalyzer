package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.blacklist.Ignored;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Item;
import unidue.ub.settings.fachref.Stockcontrol;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@StepScope
public class ExpressionProcessor implements ItemProcessor<Expression, Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ExpressionProcessor.class);

    private Stockcontrol stockcontrol;

    public ExpressionProcessor() {
    }

    @Override
    public Eventanalysis process(final Expression expression) throws Exception {
        log.info("analyzing expression  " + expression.getShelfmarkBase() + " and shelfmark " + expression.getShelfmarkBase());

        try {
            ResponseEntity<Ignored> response = new RestTemplate().getForEntity(
                    "http://localhost:8082/api/settings/stockcontrol/" + expression.getShelfmarkBase(),
                    Ignored.class,
                    0);
            if (response.getStatusCode().value() == 200) {
                Ignored ignored = response.getBody();
                if (ignored.getExpire().after(new Date()) && ignored.getType().equals("eventanalysis")) {
                    log.info("expression blacklisted");
                    return null;
                }
            }
            return calculateAnalysis(expression, stockcontrol);
        } catch (HttpClientErrorException hcee) {
            return calculateAnalysis(expression, stockcontrol);
        }
    }

    @BeforeStep
    public void retrieveStockcontrol(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.stockcontrol = (Stockcontrol) jobContext.get("stockcontrol");
        log.info("retrieved stockcontrol " + stockcontrol.toString() + " from execution context by expression processor");
    }

    private Eventanalysis calculateAnalysis(Expression expression, Stockcontrol stockcontrol) throws URISyntaxException {
        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(), stockcontrol.getMaterials());
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
        StringBuilder collections = new StringBuilder();
        HashMap<String,Integer> numberOfItems = new HashMap<>();
        Eventanalysis analysis = new EventAnalyzer().analyze(events, stockcontrol);
        for (Item item: expression.getItems()) {
            if (item.getDeletionDate() != null) {
                if (numberOfItems.containsKey(item.getCollection())) {
                    Integer count = numberOfItems.get(item.getCollection());
                    count = count +1;
                    numberOfItems.put(item.getCollection(), count);
                } else {
                    numberOfItems.put(item.getCollection(),1);
                }
            }
        }
        numberOfItems.forEach(
                (key,value) -> collections.append(String.valueOf(value)).append("* ").append(key).append(", ")
        );
        analysis.setTitleId(expression.getShelfmarkBase());
        analysis.setShelfmark(expression.getShelfmarkBase());
        analysis.setMab(expression.getBibliographicInformation().toString());
        return analysis;
    }
}
