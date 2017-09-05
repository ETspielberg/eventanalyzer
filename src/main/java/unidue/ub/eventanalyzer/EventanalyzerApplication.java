package unidue.ub.eventanalyzer;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.settings.fachref.Stockcontrol;

@SpringBootApplication
public class EventanalyzerApplication {

	@Value("${ub.statistics.settings.url}")
	private static String settingsUrl;

	public static void main(String[] args) {

		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString("stockcontrol", args[0]);
		ResponseEntity<Stockcontrol> response = new RestTemplate().getForEntity(
				settingsUrl + "/stockcontrol/" + args[0] ,
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

		SpringApplication.run(EventanalyzerApplication.class, args);
	}
}
