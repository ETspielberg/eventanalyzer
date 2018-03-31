package unidue.ub.batch.counterbuilder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class CounterbuilderConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;


    @Bean
    @StepScope
    public CounterFileReader counterFileReader() {
        return new CounterFileReader();
    }

    @Bean
    @StepScope
    public CounterProcessor counterProcessor() {
        return new CounterProcessor();
    }

    @Bean
    @StepScope
    public CounterCollectionWriter counterCollectionWriter() {
        return new CounterCollectionWriter();
    }

    @Bean
    @StepScope
    public TypeAndFormatDeterminerTasklet startingTasklet() {
        return new TypeAndFormatDeterminerTasklet();
    }

    @Bean
    public Step init() {
        return stepBuilderFactory.get("init")
                .tasklet(startingTasklet())
                .build();
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<String, CounterCollection>chunk(10000)
                .reader(counterFileReader())
                .processor(counterProcessor())
                .writer(counterCollectionWriter())
                .build();
    }

    @Bean
    public Job nrequestsJob() {
        return jobBuilderFactory.get("counterReaderJob")
                .start(init())
                .next(step())
                .build();
    }


}
