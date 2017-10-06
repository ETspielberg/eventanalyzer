package unidue.ub.batch.sushi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;
import unidue.ub.settings.fachref.Stockcontrol;
import unidue.ub.settings.fachref.Sushiprovider;

@Component
public class SushiFlowDecision implements JobExecutionDecider {

    static final String FAILED = "FAILED";

    static final String COMPLETED = "COMPLETED";

    static final String UNKNOWN = "UNKNOWN";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    SushiFlowDecision() { }

    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        Sushiprovider sushiprovider = (Sushiprovider) jobExecution.getExecutionContext().get("sushiprovider");
        if (sushiprovider == null)
            return FlowExecutionStatus.UNKNOWN;
        if (sushiprovider.getName().equals("newProvider"))
            return FlowExecutionStatus.UNKNOWN;
        else
            return FlowExecutionStatus.COMPLETED;
    }
}
