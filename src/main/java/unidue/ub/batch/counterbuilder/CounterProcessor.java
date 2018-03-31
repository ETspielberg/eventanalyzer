package unidue.ub.batch.counterbuilder;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import unidue.ub.media.analysis.Counter;
import unidue.ub.media.analysis.DatabaseCounter;
import unidue.ub.media.analysis.EbookCounter;
import unidue.ub.media.analysis.JournalCounter;

import java.util.*;

public class CounterProcessor implements ItemProcessor<String, CounterCollection> {

    private String type;

    private Map<Integer,String> datesMap;

    private Set<Integer> dateKeys;

    private Map<Integer, String> fieldMap;

    private Set<Integer> fieldKeys;

    private List<Counter> counters;

    private String delimiter;

    @Override
    public CounterCollection process(final String line) {
    counters = new ArrayList<>();
    switch (type) {
        case "database": {
            convertLineToDatabaseCounters(line);
            break;
        }
        case "journal": {
            convertLineToJournalCounters(line);
            break;
        }
        case "ebook": {
            convertLinToEbookCounters(line);
        }
    }
    return new CounterCollection(counters);
    }

    private void convertLinToEbookCounters(String line) {
        String[] parts = line.split(delimiter);
        for (Integer dateKey : dateKeys) {
            BeanWrapper wrapper = new BeanWrapperImpl(new EbookCounter());
            for (Integer fieldKey : fieldKeys) {
                wrapper.setPropertyValue(fieldMap.get(fieldKey), parts[fieldKey]);
            }
            String datesString = datesMap.get(dateKey);
            int month = Integer.parseInt(datesString.substring(0,datesString.indexOf("-")));
            wrapper.setPropertyValue("month", month);
            int year = 2000 + Integer.parseInt(datesString.substring(datesString.indexOf("-")+1));
            wrapper.setPropertyValue("year", year);
            wrapper.setPropertyValue("totalRequests",parts[dateKey]);
            counters.add((EbookCounter) wrapper.getWrappedInstance());
        }
    }

    private void convertLineToJournalCounters(String line) {
        String[] parts = line.split(delimiter);
        for (Integer dateKey : dateKeys) {
            BeanWrapper wrapper = new BeanWrapperImpl(new JournalCounter());
            for (Integer fieldKey : fieldKeys) {
                wrapper.setPropertyValue(fieldMap.get(fieldKey), parts[fieldKey]);
            }
            String datesString = datesMap.get(dateKey);
            int month = Integer.parseInt(datesString.substring(0,datesString.indexOf("-")));
            wrapper.setPropertyValue("month", month);
            int year = 2000 + Integer.parseInt(datesString.substring(datesString.indexOf("-")+1));
            wrapper.setPropertyValue("year", year);
            wrapper.setPropertyValue("totalRequests",parts[dateKey]);
            counters.add((EbookCounter) wrapper.getWrappedInstance());
        }
    }

    private void convertLineToDatabaseCounters(String line) {
        Integer activityKey = 0;
        for (Integer key : fieldKeys) {
            if (fieldMap.get(key).equals("activity"))
                activityKey = key;
        }
        Map<String,String[]> map = new HashMap<>();
        String[] parts = line.split("/");
        List<String> activityNames = new ArrayList<>();
        for (String part : parts) {
            String[] fields = part.split(delimiter);
            String activityName = fields[activityKey];
            activityNames.add(activityName);
            map.put(activityName, fields);
        }
        for (Integer dateKey : dateKeys) {
            BeanWrapper wrapper = new BeanWrapperImpl(new DatabaseCounter());
            for (Integer fieldKey : fieldKeys) {
                wrapper.setPropertyValue(fieldMap.get(fieldKey), map.get(activityNames.get(0))[fieldKey]);
            }
            String datesString = datesMap.get(dateKey);
            int month = Integer.parseInt(datesString.substring(0,datesString.indexOf("-")));
            wrapper.setPropertyValue("month", month);
            int year = 2000 + Integer.parseInt(datesString.substring(datesString.indexOf("-")+1));
            wrapper.setPropertyValue("year", year);
            for (String activityName: activityNames) {
                String propertyName = "";
                if (activityName.equals("regular searches"))
                    propertyName = "regularSearches";
                else if (activityName.equals("result clicks"))
                    propertyName = "resultClicks";
                else if (activityName.equals("record views"))
                    propertyName = "recordViews";
                else if (activityName.equals("federated and automated searches") || activityName.equals("federated searches") || activityName.equals("automated searches"))
                    propertyName = "federatedAndAutomatedSearches";
                wrapper.setPropertyValue(propertyName, map.get(activityName)[dateKey]);
                counters.add((EbookCounter) wrapper.getWrappedInstance());
            }
        }
    }


    @BeforeStep
    public void retrieveTypeAndMaps(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.type = jobContext.getString("type");
        this.delimiter = jobContext.getString("delimiter");
        this.datesMap = (Map<Integer,String>) jobContext.get("datesMap");
        dateKeys = datesMap.keySet();
        this.fieldMap = (Map<Integer,String>) jobContext.get("fieldMap");
        fieldKeys = fieldMap.keySet();
    }
}
