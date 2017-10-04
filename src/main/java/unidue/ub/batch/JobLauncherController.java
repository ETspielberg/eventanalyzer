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
    Job job;

    static String identifier = "";

    @RequestMapping("/batch/eventanalyzer")
    public void runEventanalzer(String identifier) throws Exception {
        if (identifier != null)
        this.identifier = identifier;

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("stockcontrol.identifier", identifier);
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(job,jobParameters);
    }
}
