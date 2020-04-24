package example.client;

import example.client.service.CustomerService;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableClusterDefinedRegions;
import org.springframework.geode.boot.autoconfigure.ContinuousQueryAutoConfiguration;

import java.util.List;

@SpringBootApplication(exclude = ContinuousQueryAutoConfiguration.class) // disable subscriptions
@EnableClusterDefinedRegions
public class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  @Autowired
  private CustomerService service;

  public static void main(String[] args) {
    new SpringApplicationBuilder(Client.class)
      .build()
      .run(args);
  }

  @Bean("Customer")
  ClientRegionFactoryBean testObjectRegion(GemFireCache cache) {
    ClientRegionFactoryBean<?, ?> regionFactory = new ClientRegionFactoryBean<>();
    regionFactory.setCache(cache);
    regionFactory.setShortcut(ClientRegionShortcut.PROXY);
    return regionFactory;
  }

  @Bean
  ApplicationRunner runner() {
    return args -> {
      List<String> operations = args.getOptionValues("operation");
      String operation = operations.get(0);
      String parameter1 = (args.containsOption("parameter1")) ? args.getOptionValues("parameter1").get(0) : null;
      String parameter2 = (args.containsOption("parameter2")) ? args.getOptionValues("parameter2").get(0) : null;
      switch (operation) {
      case "load":
        this.service.load(Integer.parseInt(parameter1));
        break;
      case "load-destroy":
        this.service.loadDestroy(Integer.parseInt(parameter1));
        break;
      case "dump-pdxtypes":
        this.service.dumpPdxTypes();
        break;
      case "remove-unused-pdxtypes":
        this.service.removeUnusedPdxTypes(parameter1, Boolean.parseBoolean(parameter2));
        break;
    }};
  }
}
