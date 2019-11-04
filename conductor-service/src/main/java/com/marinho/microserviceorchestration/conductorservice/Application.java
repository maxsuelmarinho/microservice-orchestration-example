package com.marinho.microserviceorchestration.conductorservice;

import com.marinho.microserviceorchestration.conductorservice.worker.TaskSampleWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class Application implements CommandLineRunner {
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);

	@Value("${spring.application.name:conductor-service}")
	public String appName;

	@Autowired
	private TaskSampleWorker taskSampleWorker;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("\n\n{} is starting Task A polling...", appName);
		taskSampleWorker.initConductorPolling();
	}
}
