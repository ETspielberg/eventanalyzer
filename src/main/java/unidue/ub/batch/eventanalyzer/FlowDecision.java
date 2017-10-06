package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;
import unidue.ub.settings.fachref.Stockcontrol;

@Component
public class FlowDecision implements JobExecutionDecider {

    static final String FAILED = "FAILED";

    static final String COMPLETED = "COMPLETED";

    static final String UNKNOWN = "UNKNOWN";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    FlowDecision() { }

    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        Stockcontrol stockcontrol = (Stockcontrol) jobExecution.getExecutionContext().get("stockcontrol");
        log.info("deciding upon stockcontrol " + stockcontrol);
        if (stockcontrol == null)
            return FlowExecutionStatus.UNKNOWN;
        log.info("deciding upon identifier " + stockcontrol.getIdentifier());
        if (stockcontrol.getIdentifier().equals("newProfile"))
            return FlowExecutionStatus.UNKNOWN;
        log.info("grouped analysis : " + stockcontrol.isGroupedAnalysis());
        if (stockcontrol.isGroupedAnalysis())
                return FlowExecutionStatus.COMPLETED;
        else
            return FlowExecutionStatus.FAILED;
    }
}
