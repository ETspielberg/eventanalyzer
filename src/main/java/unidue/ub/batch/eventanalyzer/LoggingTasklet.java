package unidue.ub.batch.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.IOException;

public class LoggingTasklet implements Tasklet {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private String message;

    public String getMessage() {
        return message;
    }

    public LoggingTasklet setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws IOException {
        log.info(message);
        return RepeatStatus.FINISHED;
    }


}
