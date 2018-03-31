package unidue.ub.batch.counterbuilder;

import unidue.ub.media.analysis.Counter;

import java.util.List;

public class CounterCollection {

    CounterCollection(List<Counter> counters) {
        this.counters = counters;
    }

    private List<Counter> counters;

    public List<Counter> getCounters() {
        return counters;
    }

    public void setCounters(List<Counter> counters) {
        this.counters = counters;
    }
}
