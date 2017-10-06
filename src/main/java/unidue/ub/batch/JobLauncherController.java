package unidue.ub.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class JobLauncherController {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job eventanalyzerJob;

    @Autowired
    Job sushiJob;

    @RequestMapping("/batch/eventanalyzer")
    public void runEventanalzer(String identifier) throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("stockcontrol.identifier", identifier);
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(eventanalyzerJob,jobParameters);
    }

    @RequestMapping("/batch/sushi")
    public void runSushiClient(String identifier, String type, String mode) throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("sushiprovider.identifier", identifier)
        .addString("type" , type)
        .addString("mode" , mode);
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(sushiJob,jobParameters);
    }
}