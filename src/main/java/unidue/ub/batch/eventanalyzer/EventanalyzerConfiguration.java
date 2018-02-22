package unidue.ub.batch.eventanalyzer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import unidue.ub.batch.DataWriter;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Status;


@Configuration
@EnableBatchProcessing
public class EventanalyzerConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public RestTemplate notationTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate stockcontrolTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate getterTemplate() {
        return new RestTemplate();
    }

    @Bean
    @StepScope
    public ManifestationReader manifestationReader() {
        return new ManifestationReader();
    }

    @Bean
    @StepScope
    public ExpressionReader expressionReader(ManifestationReader manifestationReader) {
        return new ExpressionReader(manifestationReader);

    }

    @Bean
    @StepScope
    public StockcontrolSettingTasklet startingTasklet() {
        return new StockcontrolSettingTasklet().setStatus(Status.RUNNING);
    }

    @Bean
    @StepScope
    public StockcontrolSettingTasklet finishedTasklet() {
        return new StockcontrolSettingTasklet().setStatus(Status.FINISHED);
    }

    @Bean
    @StepScope
    public ManifestationProcessor manifestationProcessor() {
        return new ManifestationProcessor();
    }

    @Bean
    @StepScope
    public ExpressionProcessor expressionProcessor() {
        return new ExpressionProcessor();
    }

    @Bean
    public FlowDecision decision() {
        return new FlowDecision();
    }

    @Bean
    @StepScope
    public DataWriter writer() {
        return new DataWriter();
    }

    @StepScope
    @Bean
    public StockcontrolInitializerTasklet stockcontrolInitializer() {
        return new StockcontrolInitializerTasklet();
    }

    @Bean
    public Job eventanalyzerJob(JobExecutionListener listener) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("eventanalyzerFlow");
        Flow flow = flowBuilder
                .start(initStockcontrol())
                .next(decision())
                .on(decision().FAILED)
                .to(manifestationFlow())
                .from(decision())
                .on(decision().COMPLETED)
                .to(expressionFlow())
                .from(decision())
                .on(decision().UNKNOWN)
                .to(step4())
                .end();
        return jobBuilderFactory.get("eventanalyzerJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(flow)
                .end()
                .build();
    }

    @Bean
    Flow manifestationFlow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("manifestationFlow");
        return flowBuilder
                .start(step1())
                .next(step2Manifestation())
                .next(step3()).end();
    }

    @Bean
    Flow expressionFlow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("expressionFlow");
        return flowBuilder
                .start(step1())
                .next(step2Expression())
                .next(step3()).end();
    }

    @Bean
    public Step initStockcontrol() {
        return stepBuilderFactory.get("init")
                .tasklet(stockcontrolInitializer())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(startingTasklet())
                .build();
    }

    @Bean
    public Step step2Expression() {
        return stepBuilderFactory.get("step2Expression")
                .<Expression, Eventanalysis>chunk(10)
                .reader(expressionReader(manifestationReader()))
                .processor(expressionProcessor())
                .writer(writer())
                .build();
    }

    @Bean
    public Step step2Manifestation() {
        return stepBuilderFactory.get("step2")
                .<Manifestation, Eventanalysis>chunk(10)
                .reader(manifestationReader())
                .processor(manifestationProcessor())
                .writer(writer())
                .build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet(finishedTasklet())
                .build();
    }

    @Bean
    public Step step4() {
        return stepBuilderFactory.get("step4")
                .tasklet(new LoggingTasklet().setMessage("initialized"))
                .build();
    }

}
