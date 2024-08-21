package pupket.togedogserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableWebSecurity
public class TogedogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TogedogServerApplication.class, args);
    }

}
