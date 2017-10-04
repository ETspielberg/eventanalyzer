package unidue.ub.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventanalyzerApplication {

	private final static Logger log = LoggerFactory.getLogger(EventanalyzerApplication.class);

	static String identifier;

	public static void main(String[] args) {
		identifier = "";
		if (args.length > 0) {
			identifier = args[0];
		}
		log.info("batch application found identifier " + identifier);
		SpringApplication.run(EventanalyzerApplication.class, args);
	}
}
