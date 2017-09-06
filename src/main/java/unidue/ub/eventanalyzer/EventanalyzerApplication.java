package unidue.ub.eventanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import unidue.ub.settings.fachref.Stockcontrol;

@SpringBootApplication
public class EventanalyzerApplication {

	private final static Logger log = LoggerFactory.getLogger(EventanalyzerApplication.class);

	private static String identifier;

	public static void main(String[] args) {
		identifier = "";
		if (args.length > 0) {
			identifier = args[0];
		}
		SpringApplication.run(EventanalyzerApplication.class, args);
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
