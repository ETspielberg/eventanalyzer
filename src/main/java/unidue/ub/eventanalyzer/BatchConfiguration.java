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

    public boolean groupedAnalysis = false;

@Bean
@StepScope
public JobParameters jobParameters(@Value("#{jobParameters['stockcontrol.identifer']}") String identifier){
    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("stockcontrol", identifier);
    ResponseEntity<Stockcontrol> response = new RestTemplate().getForEntity(
            settingsUrl + "/stockcontrol/" + identifier ,
            Stockcontrol.class
    );
    Stockcontrol stockcontrol = response.getBody();
    builder.addString("collections", stockcontrol.getCollections());
    builder.addString("materials", stockcontrol.getMaterials());
    builder.addString("deletionMailBcc", stockcontrol.getDeletionMailBcc());
    builder.addString("systemCode", stockcontrol.getSystemCode());
    builder.addString("systemCode", stockcontrol.getSubjectID());
    builder.addLong("yearsToAverage",(long) stockcontrol.getYearsToAverage());
    builder.addDouble("blacklistExpire", stockcontrol.getBlacklistExpire());
    builder.addLong("minimumDaysOfRequest",(long) stockcontrol.getMinimumDaysOfRequest());
    builder.addLong("minimumYears",(long) stockcontrol.getMinimumYears());
    builder.addDouble("staticBuffer", stockcontrol.getStaticBuffer());
    builder.addDouble("variableBuffer", stockcontrol.getVariableBuffer());
    builder.addLong("yearsOfRequests",(long) stockcontrol.getYearsOfRequests());
    groupedAnalysis = stockcontrol.isGroupedAnalysis();
    builder.addString("groupedAnalysis", String.valueOf(groupedAnalysis));
    return builder.toJobParameters();
}

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
        ManifestationReader reader = new ManifestationReader();
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate());
        return reader;
    }

    @Bean
    @StepScope
    public ExpressionReader expressionReader() {
        ExpressionReader reader = new ExpressionReader();
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate());
        return reader;
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
    public AnalysisWriter writer() {
        return new AnalysisWriter();
    }

    @Bean
    @StepScope
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
        if (groupedAnalysis) {
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
