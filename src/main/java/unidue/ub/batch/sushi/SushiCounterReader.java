package unidue.ub.batch.sushi;

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
import unidue.ub.media.tools.CounterTools;
import unidue.ub.settings.fachref.Sushiprovider;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@StepScope
public class SushiCounterReader<SoapMessage> implements ItemReader<Object> {

    private Sushiprovider sushiprovider;
    @Value("#{jobParameters['sushi.mode'] ?: 'update'}")
    private String mode;
    @Value("#{jobParameters['sushi.type'] ?: 'JR1'}")
    private String type;
    @Value("#{jobParameters['sushi.year'] ?: 2017}")
    private Integer year;
    @Value("#{jobParameters['sushi.month'] ?: 1}")
    private Integer month;
    private SOAPMessage soapMessage;
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
                for (int i = 1; i<= 12; i++) {
                    LocalDateTime start = LocalDateTime.of(year,i,1,0,0);
                    LocalDateTime end = start.plusMonths(1).minusDays(1);
                    List<Counter> countersFound = executeSushiClient(sushiClient,start,end);
                    addCountersToList(countersFound);
                }
            }
            case "month": {
                    LocalDateTime start = LocalDateTime.of(year,month,1,0,0);
                    LocalDateTime end = start.plusMonths(1).minusDays(1);
                    List<Counter> countersFound = executeSushiClient(sushiClient,start,end);
                    addCountersToList(countersFound);
                }
        }
        log.info("collected " + counters.size() + " counters in total");
        collected = true;
    }

    private void addCountersToList(List<Counter> countersFound) {
        if (countersFound != null) {
            if (countersFound.size() != 0) {
                counters.addAll(countersFound);
                log.info("added " + countersFound.size() + " counter statistics.");
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
        soapMessage = sushiClient.getResponse();
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
