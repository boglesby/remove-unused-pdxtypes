package example.client.service;

import example.PdxTypeStatus;
import example.client.function.OnServerFunctions;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

  @Autowired
  private GemfireTemplate customerTemplate;

  @Autowired
  private OnServerFunctions functions;

  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

  public void load(int numEntries) {
    long start, end;
    start = System.currentTimeMillis();
    for (int i=0; i<numEntries; i++) {
      PdxInstance customer = createCustomer(i);
      this.customerTemplate.put(String.valueOf(i), customer);
      logger.info("Loaded {}", customer);
    }
    end = System.currentTimeMillis();
    logger.info("Loaded {} PdxInstances in {} ms", numEntries, end-start);
  }

  public void loadDestroy(int numEntries) {
    long start, end;
    start = System.currentTimeMillis();
    for (int i=0; i<numEntries; i++) {
      String key = String.valueOf(i);
      PdxInstance customer = createCustomer(i);
      this.customerTemplate.put(key, customer);
      logger.info("Loaded {}", customer);
      if (i % 2 == 0) {
        this.customerTemplate.remove(key);
        logger.info("Destroyed {}", customer);
      }
    }
    end = System.currentTimeMillis();
    logger.info("Loaded {} PdxInstances in {} ms", numEntries, end-start);
  }

  public void dumpPdxTypes() {
    logger.info("Dumped PdxTypes result={}", this.functions.dumpPdxTypes());
  }

  public void removeUnusedPdxTypes(String regionNames, boolean simulate) {
    long start, end;
    start = System.currentTimeMillis();
    PdxTypeStatus result = this.functions.removeUnusedPdxTypes(regionNames, simulate);
    end = System.currentTimeMillis();
    StringBuilder builder = new StringBuilder();
    builder
      .append("Removed unused PdxTypes in ")
      .append(end-start)
      .append(" ms")
      .append(" with result=")
      .append(result);
    logger.info(builder.toString());
  }

  private PdxInstance createCustomer(int index) {
    String jsonCustomer = "{"
      + "\"firstName"+index+"\": \"John\","
      + "\"lastName\": \"Johnson\","
      + "\"age\": 25,"
      + "\"timestamp\": 1586811161843,"
      + "\"donor\": true,"
      + "\"accounts\": [\"acc1\",\"acc2\",\"acc3\"],"
      + "\"address\":"
      + "{"
      + "\"streetAddress\": \"123 Main Street\","
      + "\"city\": \"New York\","
      + "\"state\": \"NY\","
      + "\"postalCode\": \"10021\""
      + "},"
      + "\"phoneNumber\":"
      + "["
      + "{"
      + " \"type\": \"home\","
      + "\"number\": \"212-555-1234\""
      + "},"
      + "{"
      + " \"type\": \"fax\","
      + "\"number\": \"646-555-1234\""
      + "}"
      + "]"
      + "}";
    return JSONFormatter.fromJSON(jsonCustomer);
  }
}
