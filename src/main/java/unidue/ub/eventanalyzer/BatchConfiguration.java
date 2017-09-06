package unidue.ub.eventanalyzer;

import org.springframework.batch.core.*;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Manifestation;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

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
    public ManifestationReader manifestationReader() {
        ManifestationReader reader = new ManifestationReader(EventanalyzerApplication.stockcontrol());
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate());
        return reader;
    }

    @Bean
    public ExpressionReader expressionReader() {
        return new ExpressionReader(EventanalyzerApplication.stockcontrol());
    }

    @Bean
    public ManifestationProcessor manifestationProcessor() {
        return new ManifestationProcessor(EventanalyzerApplication.stockcontrol());
    }

    @Bean
    public ExpressionProcessor expressionProcessor() {
        return new ExpressionProcessor(EventanalyzerApplication.stockcontrol());
    }

    @Bean
    public AnalysisWriter writer() {
        return new AnalysisWriter();
    }

    @Bean
    public Job eventanalyzerJob(JobExecutionListener listener) {
        return jobBuilderFactory.get("eventanalyzerJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }


    @Bean
    public Step step1() {
        Step step;
        if (EventanalyzerApplication.stockcontrol().isGroupedAnalysis()) {
            step = stepBuilderFactory.get("step1")
                    .<Expression, Eventanalysis>chunk(10)
                    .reader(expressionReader())
                    .processor(expressionProcessor())
                    .writer(writer())
                    .build();
        } else {
            step = stepBuilderFactory.get("step1")
                    .<Manifestation, Eventanalysis>chunk(10)
                    .reader(manifestationReader())
                    .processor(manifestationProcessor())
                    .writer(writer())
                    .build();
        }
        return step;
    }
}
