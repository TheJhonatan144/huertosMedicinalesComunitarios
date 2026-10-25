package ec.edu.epn.huertos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HuertosApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuertosApplication.class, args);
    }
}
