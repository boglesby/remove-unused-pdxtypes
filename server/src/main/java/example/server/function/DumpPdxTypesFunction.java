package example.server.function;

import example.PdxTypeStatus;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.pdx.internal.PdxType;
import org.apache.geode.pdx.internal.PeerTypeRegistration;

import java.util.Map;
import java.util.stream.Collectors;

public class DumpPdxTypesFunction implements Function, Declarable {

  private final Cache cache;

  public DumpPdxTypesFunction() {
    this.cache = CacheFactory.getAnyInstance();
  }

  public void execute(FunctionContext context) {
    Map<Integer,PdxType> pdxTypes = getAllPdxTypes();
    StringBuilder builder = new StringBuilder();
    builder.append("The cache contains ").append(pdxTypes.size()).append(" PdxTypes:");
    pdxTypes.forEach(
      (key, value) -> builder
        .append("\n\t")
        .append("key=")
        .append(key)
        .append("; id=")
        .append(value.getTypeId())
        .append("; num=")
        .append(value.getTypeNum())
        .append("; pdxType=")
        .append(value)
    );
    this.cache.getLogger().info(builder.toString());
    context.getResultSender().lastResult(new PdxTypeStatus(pdxTypes.size(), -1, -1));
  }

  private Map getAllPdxTypes() {
    return this.cache.getRegion(PeerTypeRegistration.REGION_NAME).entrySet()
      .stream()
      .filter(entry -> entry.getValue() instanceof PdxType)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public String getId() {
    return getClass().getSimpleName();
  }
}
