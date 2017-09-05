package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Event;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Item;

import java.util.ArrayList;
import java.util.List;

@StepScope
public class ExpressionProcessor implements ItemProcessor<Expression,Eventanalysis> {

    private static final Logger log = LoggerFactory.getLogger(ExpressionProcessor.class);

    @Value("#{jobParameters['collections']}")
    private String collections;

    @Value("#{jobParameters['materials']}")
    private String materials;

    @Override
    public Eventanalysis process(final Expression expression) throws Exception {
        log.info("analyzing manifestation " + expression.getShelfmarkBase() + " and shelfmark " + expression.getShelfmarkBase());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(collections,materials);
        for (Item item : expression.getItems()) {
            if (itemFilter.matches(item))
                events.addAll(item.getEvents());
        }

        EventAnalyzer analyzer = new EventAnalyzer(events,expression.getShelfmarkBase());
        return analyzer.getEventanalysis();
    }
}
