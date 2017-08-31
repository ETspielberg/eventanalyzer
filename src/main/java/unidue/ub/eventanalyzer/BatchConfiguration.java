package unidue.ub.eventanalyzer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Value("#{jobParameters['identifer']}")
    private String identifier;

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
    public Stockcontrol stockcontrol() {;
        ResponseEntity<Stockcontrol> response = new RestTemplate().getForEntity(
                settingsUrl + "/stockcontrol/" + identifier ,
                Stockcontrol.class
        );
        Stockcontrol stockcontrol = response.getBody();
        return stockcontrol;
    }

    @Bean
    public RestTemplate getterTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ManifestationReader reader() {
        ManifestationReader reader = new ManifestationReader();
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate())
                .setStockcontrol(stockcontrol());
        return reader;
    }

    @Bean
    public ManifestationProcessor processor() {
        return new ManifestationProcessor();
    }

    @Bean
    public AnalysisWriter writer() {
        return writer();
    }

    @Bean
    public Job importUserJob(JobExecutionListener listener) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Manifestation, Eventanalysis> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
}
