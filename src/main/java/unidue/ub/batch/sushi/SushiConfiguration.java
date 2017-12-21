package unidue.ub.batch.sushi;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unidue.ub.batch.DataWriter;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Status;

@Configuration
@EnableBatchProcessing
public class SushiConfiguration {

    @Value("${ub.statistics.settings.url}")
    String settingsUrl;

    @Value("${ub.statistics.data.url}")
    String dataUrl;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public SushiCounterReader reader() {
        return new SushiCounterReader();
    }

    @Bean
    @StepScope
    public DataWriter writer() {
        DataWriter writer = new DataWriter();
        writer.setDataUrl(dataUrl);
        return writer;
    }

    @Bean
    @StepScope
    public SushiproviderInitializerTasklet sushiproviderIntializer() {
        SushiproviderInitializerTasklet sushiproviderInitializerTasklet = new SushiproviderInitializerTasklet();
        sushiproviderInitializerTasklet.setSettingsUrl(settingsUrl);
        return sushiproviderInitializerTasklet;
    }

    @Bean
    @StepScope
    public SushiproviderSettingTasklet sushiStartingTasklet() {
        SushiproviderSettingTasklet tasklet = new SushiproviderSettingTasklet();
        tasklet.setSettingsUrl(settingsUrl);
        return tasklet.setStatus(Status.RUNNING);
    }

    @Bean
    @StepScope
    public SushiproviderSettingTasklet sushiFinishedTasklet() {
        SushiproviderSettingTasklet tasklet = new SushiproviderSettingTasklet();
        tasklet.setSettingsUrl(settingsUrl);
        return tasklet.setStatus(Status.FINISHED);
    }

    @Bean
    public SushiFlowDecision sushiDecision() {
        return new SushiFlowDecision();
    }

    @Bean
    public Step sushiproviderSetStart() {
        return stepBuilderFactory.get("startStep")
        .tasklet(sushiStartingTasklet())
        .build();
    }

    @Bean
    public Step sushiproviderSetFinished() {
        return stepBuilderFactory.get("endStep")
                .tasklet(sushiFinishedTasklet())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<Manifestation, Eventanalysis>chunk(100)
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean
    public Step init() {
        return stepBuilderFactory.get("init")
                .tasklet(sushiproviderIntializer())
                .build();
    }

    @Bean
    public Flow sushiFlow() {
        FlowBuilder<Flow> flowBuilder  = new FlowBuilder<>("manifestationFlow");
        return flowBuilder
                .start(sushiproviderSetStart())
                .next(step())
                .next(sushiproviderSetFinished()).end();
    }

    @Bean
    public Job sushiJob() {
        return jobBuilderFactory.get("sushiJob")
                .incrementer(new RunIdIncrementer())
                .start(init())
                .next(sushiDecision())
                .on(sushiDecision().COMPLETED)
                .to(sushiFlow())
                .from(sushiDecision())
                .on(sushiDecision().UNKNOWN)
                .end()
                .build()
                .build();

    }
}
