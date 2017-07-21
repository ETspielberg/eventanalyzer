package unidue.ub.eventanalyzer;

import unidue.ub.media.analysis.Eventanalysis;

import org.springframework.batch.item.ItemProcessor;
import unidue.ub.media.monographs.Event;
import unidue.ub.settings.fachref.Stockcontrol;

import java.util.List;

public class EventlistProcessor implements ItemProcessor<List<Event>,Eventanalysis> {

    @Override
    public Eventanalysis process(final List<Event> events) {
        EventAnalyzer eventAnalyzer = new EventAnalyzer(events,"",new Stockcontrol());
        return eventAnalyzer.getEventanalysis();
    }
}
