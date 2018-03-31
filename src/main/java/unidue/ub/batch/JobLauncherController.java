package unidue.ub.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/batch")
public class JobLauncherController {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job eventanalyzerJob;

    @Autowired
    Job sushiJob;

    @Autowired
    Job nrequestsJob;

    @Autowired
    Job counterbuilderJob;

    @RequestMapping("/eventanalyzer")
    public ResponseEntity<?> runEventanalzer(String identifier) throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("stockcontrol.identifier", identifier).addLong("time",System.currentTimeMillis()).toJobParameters();;
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(eventanalyzerJob, jobParameters);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    @RequestMapping("/sushi")
    public ResponseEntity<?> runSushiClient(String identifier, String type, String mode, Long year, Long month) throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("sushiprovider.identifier", identifier)
                .addString("sushi.type", type)
                .addString("sushi.mode", mode)
                .addLong("sushi.year", year)
                .addLong("sushi.month", month)
                .addLong("time",System.currentTimeMillis()).toJobParameters();
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(sushiJob, jobParameters);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    @RequestMapping("/nrequests")
    public ResponseEntity<?> runNrequestsCollector() throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("date", new Date()).addLong("time",System.currentTimeMillis()).toJobParameters();;
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(nrequestsJob, jobParameters);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    @GetMapping("/counterbuilder")
    public void buildCounter(String filename) throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("counter.file.name", filename)
                .addLong("time",System.currentTimeMillis()).toJobParameters();
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();
        jobLauncher.run(counterbuilderJob, jobParameters);
    }
}
