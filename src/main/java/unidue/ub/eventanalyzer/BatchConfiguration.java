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

import static unidue.ub.eventanalyzer.EventanalyzerApplication.stockcontrol;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

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

    @Value("${ub.statistics.settings.url}")
    private String settingsUrl;

    @Value("${ub.statistics.getter.url}")
    private String gettersUrl;

    @Value("${ub.statistics.data.url}")
    private String dataUrl;

    @Bean
    public ManifestationReader manifestationReader() {
        ManifestationReader reader = new ManifestationReader(stockcontrol());
        reader.setNotationTemplate(notationTemplate())
                .setRestTemplate(getterTemplate())
                .setGetterUrl(gettersUrl)
                .setSettingsUrl(settingsUrl);
        return reader;
    }

    @Bean
    public ExpressionReader expressionReader() {
        ExpressionReader reader =  new ExpressionReader(stockcontrol(),manifestationReader());
        return reader;

    }

    @Bean
    public StockcontrolSettingTasklet startingTasklet() {
        StockcontrolSettingTasklet tasklet = new StockcontrolSettingTasklet();
        tasklet.setSettingsUrl(settingsUrl);
        tasklet.setStockcontrol(stockcontrol());
        return tasklet.setStatus("running");
    }

    @Bean
    public StockcontrolSettingTasklet finishedTasklet() {
        StockcontrolSettingTasklet tasklet = new StockcontrolSettingTasklet();
        tasklet.setSettingsUrl(settingsUrl);
        tasklet.setStockcontrol(stockcontrol());
        return tasklet.setStatus("finished");
    }

    @Bean
    public ManifestationProcessor manifestationProcessor() {
        return new ManifestationProcessor(stockcontrol());
    }

    @Bean
    public ExpressionProcessor expressionProcessor() {
        return new ExpressionProcessor(stockcontrol());
    }

    @Bean
    public AnalysisWriter writer() {
        AnalysisWriter writer =  new AnalysisWriter();
        writer.setDataUrl(dataUrl);
        return writer;
    }

    @Bean
    public Job eventanalyzerJob(JobExecutionListener listener) {
        return jobBuilderFactory.get("eventanalyzerJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step1())
                .next(step2())
                .next(step3())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(startingTasklet())
                .build();
    }

    @Bean
    public Step step2() {
        Step step;
        if (stockcontrol().isGroupedAnalysis()) {
            step = stepBuilderFactory.get("step2")
                    .<Expression, Eventanalysis>chunk(10)
                    .reader(expressionReader())
                    .processor(expressionProcessor())
                    .writer(writer())
                    .build();
        } else {
            step = stepBuilderFactory.get("step2")
                    .<Manifestation, Eventanalysis>chunk(10)
                    .reader(manifestationReader())
                    .processor(manifestationProcessor())
                    .writer(writer())
                    .build();
        }
        return step;
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet(finishedTasklet())
                .build();
    }

}
