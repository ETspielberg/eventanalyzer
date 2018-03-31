package unidue.ub.batch.counterbuilder;

import unidue.ub.batch.DataWriter;
import unidue.ub.media.analysis.Counter;

import org.springframework.batch.item.ItemWriter;
import java.util.List;

public class CounterCollectionWriter implements ItemWriter {

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
