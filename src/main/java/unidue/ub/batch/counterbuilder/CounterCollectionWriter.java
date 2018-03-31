package unidue.ub.batch.counterbuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.ub.batch.DataWriter;
import unidue.ub.media.analysis.Counter;

import org.springframework.batch.item.ItemWriter;
import java.util.List;

public class CounterCollectionWriter implements ItemWriter {

    private Logger log = LoggerFactory.getLogger(CounterCollectionWriter.class);
    @Override
    public void write(List list) throws Exception {
        DataWriter writer = new DataWriter();
        List<CounterCollection> counterCollections = (List<CounterCollection>) list;

        for (CounterCollection counterCollection : counterCollections) {
            List<Counter> listCounters = counterCollection.getCounters();
            writer.write(listCounters);
        }
    }
}
