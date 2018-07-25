package unidue.ub.batch.sushi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import unidue.ub.media.analysis.Counter;
import unidue.ub.media.analysis.CounterLog;
import unidue.ub.media.tools.CounterTools;
import unidue.ub.settings.fachref.Sushiprovider;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@StepScope
public class SushiCounterReader implements ItemReader<Counter> {

    private Sushiprovider sushiprovider;
    @Value("#{jobParameters['sushi.mode'] ?: 'update'}")
    private String mode;
    @Value("#{jobParameters['sushi.type'] ?: 'JR1'}")
    private String type;
    @Value("#{jobParameters['sushi.year'] ?: 2017}")
    private int year;
    @Value("#{jobParameters['sushi.month'] ?: 1}")
    private int month;
    private List<Counter> counters;
    private boolean collected = false;

    private static Logger log = LoggerFactory.getLogger(SushiCounterReader.class);

    SushiCounterReader() {
    }

    @Override
    public Counter read() throws JDOMException, SOAPException, IOException {
        if (!collected)
            collectCounters();
        if (!counters.isEmpty())
            return counters.remove(0);
        return null;
    }

    private void collectCounters() throws JDOMException, SOAPException, IOException {
        SushiClient sushiClient = new SushiClient();
        sushiClient.setProvider(sushiprovider);
        sushiClient.setReportType(type);
        LocalDateTime TODAY = LocalDateTime.now();
        counters = new ArrayList<>();
        int timeshift;
        if (TODAY.getDayOfMonth() < 15)
            timeshift = 3;
        else
            timeshift = 2;
        switch (mode) {
            case "update": {
                counters = executeSushiClient(sushiClient, timeshift);
                break;
            }
            case "full": {
                while (TODAY.minusMonths(timeshift).getYear() >= 2017) {
                    List<Counter> countersFound = executeSushiClient(sushiClient, timeshift);
                    addCountersToList(countersFound);
                    timeshift += 1;
                }
            }
            case "year": {
                for (int i = month; i<= 12; i++) {
                    List<Counter> countersFound = retrieveCounters(i, sushiClient);
                    addCountersToList(countersFound);
                }
            }
            case "month": {
                List<Counter> countersFound = retrieveCounters(month, sushiClient);
                addCountersToList(countersFound);
            }
        }

        log.info("collected " + counters.size() + " " + type + "-counters for SUSHI provider " + sushiprovider.getName());
        collected = true;
    }

    private List<Counter> retrieveCounters(int monthToBeCollected, SushiClient sushiClient) {
        CounterLog counterLog = new CounterLog();
        counterLog.setSushiprovider(sushiprovider.getName());
        counterLog.setYear(year);
        counterLog.setMonth(monthToBeCollected);
        counterLog.setReportType(type);
        LocalDateTime start = LocalDateTime.of(year, monthToBeCollected,1,0,0);
        LocalDateTime end = start.plusMonths(1).minusDays(1);
        try {
            List<Counter> countersFound = executeSushiClient(sushiClient,start,end);
            counterLog.setStatus("SUCCESS");
            counterLog.setComment("collected " + countersFound.size() + " " + type + "-counters");
            saveCounterLog(counterLog);
            addCountersToList(countersFound);
            return countersFound;
        } catch (Exception e) {
            counterLog.setError(e.getMessage());
            counterLog.setStatus("ERROR");
            saveCounterLog(counterLog);
            return null;
        }
    }

    private int saveCounterLog(CounterLog counterLog) {
        try {
            String json = new ObjectMapper().writeValueAsString(counterLog);
            HttpClient client = new HttpClient();
            PostMethod post = new PostMethod("http://localhost:8082/api/data/counterlog");
            RequestEntity entity = new StringRequestEntity(json, "application/json", null);
            post.setRequestEntity(entity);
            return client.executeMethod(post);
        } catch (JsonProcessingException e) {
            log.warn("could not convert counter log to json.");
            return 0;
        } catch (UnsupportedEncodingException e) {
            log.warn("could not post counter log.");
            return 0;
        } catch (IOException e) {
            log.warn("could not post counter log");
            return 0;
        }
    }

    private void addCountersToList(List<Counter> countersFound) {
        if (countersFound != null) {
            if (countersFound.size() != 0) {
                counters.addAll(countersFound);
                log.info("added " + countersFound.size() + " counterbuilder statistics.");
            }
        } else {
            log.warn("no counters from conversion!");
        }
    }

    private List<Counter> executeSushiClient(SushiClient sushiClient, int timeshift) throws JDOMException, SOAPException, IOException {
        LocalDateTime start = LocalDateTime.now().minusMonths(timeshift).withDayOfMonth(1);
        LocalDateTime end = LocalDateTime.now().minusMonths(timeshift - 1).withDayOfMonth(1).minusDays(1);
        return executeSushiClient(sushiClient, start, end);
    }

    private List<Counter> executeSushiClient(SushiClient sushiClient, LocalDateTime start, LocalDateTime end) throws JDOMException, SOAPException, IOException {
        List<Counter> countersFound = new ArrayList<>();
        sushiClient.setStartTime(start);
        sushiClient.setEndTime(end);
        SOAPMessage soapMessage = sushiClient.getResponse();
        if (soapMessage != null) {
            countersFound = (List<Counter>) CounterTools.convertSOAPMessageToCounters(soapMessage);
        }
        else
            log.warn("no SOAP response!");
        return countersFound;
    }

    @BeforeStep
    public void retrieveSushiprovider(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.sushiprovider = (Sushiprovider) jobContext.get("sushiprovider");
    }
}
