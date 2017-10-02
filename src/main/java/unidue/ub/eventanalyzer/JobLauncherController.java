package unidue.ub.eventanalyzer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import unidue.ub.settings.fachref.Stockcontrol;

@Controller
public class JobLauncherController {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    private static String identifier;

    @RequestMapping("/batch/eventanalyzer")
    public void runEventanalzer(String identifier) throws Exception {
        this.identifier = identifier;
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("identifier", identifier);
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(job,jobParameters);
    }

    @Bean
    public static Stockcontrol stockcontrol() {
        ResponseEntity<Stockcontrol> response = new RestTemplate().getForEntity(
                "http://localhost:11300/stockcontrol/" + identifier ,
                Stockcontrol.class
        );
        return response.getBody();
    }
}
