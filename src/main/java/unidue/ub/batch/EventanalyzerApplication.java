package unidue.ub.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class EventanalyzerApplication extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        new SpringApplication(EventanalyzerApplication.class).run(args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable().csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        http.authorizeRequests()
                .anyRequest().hasIpAddress("127.0.0.1").anyRequest().permitAll().and()
                .authorizeRequests()
                .anyRequest().authenticated().anyRequest().permitAll();
    }
}
