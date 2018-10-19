package io.github.eja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class JobApp {
  public JobApp() {}

  public static void main(String[] args) {
    ConfigurableApplicationContext applicationContext = SpringApplication.run(JobApp.class, args);
    while (applicationContext.isRunning()){}
  }
}
