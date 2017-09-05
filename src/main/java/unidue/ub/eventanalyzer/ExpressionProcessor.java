package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
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

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.data.url}")
    private String dataURL;

    @Value("${ub.statistics.status.student}")
    private String studentUser;

    @Value("${ub.statistics.status.intern}")
    private String internUser;

    @Value("${ub.statistics.status.extern}")
    private String externUser;

    @Value("${ub.statistics.status.happ}")
    private String happUser;

    @Value("${ub.statistics.status.lendable}")
    private String lendable;

    private Stockcontrol stockcontrol;

    @Override
    public Eventanalysis process(final Expression expression) throws Exception {
        log.info("analyzing manifestation " + expression.getShelfmarkBase() + " and shelfmark " + expression.getShelfmarkBase());

        List<Event> events = new ArrayList<>();
        ItemFilter itemFilter = new ItemFilter(stockcontrol.getCollections(),stockcontrol.getMaterials());
        for (Item item : expression.getItems()) {
            if (itemFilter.matches(item))
                events.addAll(item.getEvents());
        }

        EventAnalyzer analyzer = new EventAnalyzer(events,expression.getShelfmarkBase(),stockcontrol);
        return analyzer.getEventanalysis();
    }

    public ExpressionProcessor setStockcontrol(Stockcontrol stockcontrol) {
        this.stockcontrol = stockcontrol;
        return this;
    }
}
