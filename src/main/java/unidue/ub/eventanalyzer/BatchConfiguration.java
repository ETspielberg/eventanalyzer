package unidue.ub.eventanalyzer;

import org.springframework.batch.core.*;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.analysis.Eventanalysis;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.logging.Logger;

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
    @StepScope
    public Stockcontrol stockcontrol(@Value("#{jobParameters['stockcontrol.identifer']}") String identifier) {
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
    @StepScope
    public ManifestationReader manifestationReader(@Value("#{jobParameters['identifer']}") String identifier) {
        ManifestationReader reader = new ManifestationReader();
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate())
                .setStockcontrol(stockcontrol(identifier));
        return reader;
    }

    @Bean
    @StepScope
    public ExpressionReader expressionReader(@Value("#{jobParameters['identifer']}") String identifier) {
        ExpressionReader reader = new ExpressionReader();
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate())
                .setStockcontrol(stockcontrol(identifier));
        return reader;
    }

    @Bean
    @StepScope
    public ManifestationProcessor manifestationProcessor(@Value("#{jobParameters['identifer']}") String identifier) {
        ManifestationProcessor processor = new ManifestationProcessor()
                .setStockcontrol(stockcontrol(identifier));
        return processor;
    }

    @Bean
    @StepScope
    public ExpressionProcessor expressionProcessor(@Value("#{jobParameters['identifer']}") String identifier) {
        ExpressionProcessor processor = new ExpressionProcessor()
                .setStockcontrol(stockcontrol(identifier));
        return processor;
    }

    @Bean
    public AnalysisWriter writer() {
        return new AnalysisWriter();
    }

    @Bean
    @StepScope
    public Job eventanalyzerJob(JobExecutionListener listener, @Value("#{jobParameters['identifer']}") String identifier) {
        return jobBuilderFactory.get("eventanalyzerJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1(identifier))
                .end()
                .build();
    }

    @Bean
    public Step step1(@Value("#{jobParameters['identifer']}") String identifier) {
        Step step;
        if (stockcontrol(identifier).isGroupedAnalysis()) {
            step = stepBuilderFactory.get("step1")
                    .<Expression, Eventanalysis>chunk(10)
                    .reader(expressionReader(identifier))
                    .processor(expressionProcessor(identifier))
                    .writer(writer())
                    .build();
        } else {
            step = stepBuilderFactory.get("step1")
                    .<Manifestation, Eventanalysis>chunk(10)
                    .reader(manifestationReader(identifier))
                    .processor(manifestationProcessor(identifier))
                    .writer(writer())
                    .build();
        }
        return step;
    }
}
