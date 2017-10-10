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

import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.List;

@StepScope
public class SushiCounterReader<SoapMessage> implements ItemReader<Object> {

    SushiCounterReader( ) {}

    private Sushiprovider sushiprovider;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{jobParameters['sushi.mode'] ?: 'update'}")
    private String mode;

    @Value("#{jobParameters['sushi.type'] ?: 'JR1'}")
    private String type;

    private SOAPMessage soapMessage;

    private List<Counter> counters;

    @Override
    public Counter read() throws JDOMException, SOAPException, IOException {
        SushiClient sushiClient = new SushiClient();
        sushiClient.setProvider(sushiprovider);
        sushiClient.setReportType(type);
        switch (mode) {
            case "update" : {
                LocalDateTime TODAY  = LocalDateTime.now();
                int timeshift;
                if (TODAY.getDayOfMonth() < 15)
                    timeshift = 3;
                else
                    timeshift = 2;
                sushiClient.setStartTime(LocalDateTime.now().minusMonths(timeshift).withDayOfMonth(1));
                sushiClient.setEndTime(LocalDateTime.now().minusMonths(timeshift-1).withDayOfMonth(1).minusDays(1));
                break;
            }
        }
        if (soapMessage == null) {
            soapMessage = sushiClient.getResponse();
            log.info("retrieving " + type + " report from sushi provider");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapMessage.writeTo(out);
            log.info(out.toString());
            counters = (List<Counter>) CounterTools.convertSOAPMessageToCounters(soapMessage);
        }
        if (!counters.isEmpty())
            return counters.remove(0);

        return null;

    }

    @BeforeStep
    public void retrieveSushiprovider(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.sushiprovider = (Sushiprovider) jobContext.get("sushiprovider");
    }
}
