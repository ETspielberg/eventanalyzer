package unidue.ub.batch.sushi;

import org.jdom2.JDOMException;
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
    private SOAPMessage soapMessage;
    private List<Counter> counters;
    private boolean collected = false;

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
                while (TODAY.minusMonths(timeshift).getYear() >= 2000) {
                    List<Counter> countersFound = executeSushiClient(sushiClient, timeshift);
                    if (countersFound != null)
                        if (countersFound.size() != 0)
                            counters.addAll(countersFound);
                    timeshift += 1;
                }
            }
        }
        collected = true;
    }

    private List<Counter> executeSushiClient(SushiClient sushiClient, int timeshift) throws JDOMException, SOAPException, IOException {
        List<Counter> countersFound = new ArrayList<>();
        sushiClient.setStartTime(LocalDateTime.now().minusMonths(timeshift).withDayOfMonth(1));
        sushiClient.setEndTime(LocalDateTime.now().minusMonths(timeshift - 1).withDayOfMonth(1));
        soapMessage = sushiClient.getResponse();
        if (soapMessage != null)
            countersFound = (List<Counter>) CounterTools.convertSOAPMessageToCounters(soapMessage);
        return countersFound;
    }

    @BeforeStep
    public void retrieveSushiprovider(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.sushiprovider = (Sushiprovider) jobContext.get("sushiprovider");
    }
}
