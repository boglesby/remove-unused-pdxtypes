package example.server.function;

import example.PdxTypeStatus;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;

import org.apache.geode.pdx.internal.PdxInstanceImpl;
import org.apache.geode.pdx.internal.PdxField;
import org.apache.geode.pdx.internal.PdxType;
import org.apache.geode.pdx.internal.PeerTypeRegistration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

public class RemoveUnusedPdxTypesFunction implements Function, Declarable {

  private final Cache cache;

  public RemoveUnusedPdxTypesFunction() {
    this.cache = CacheFactory.getAnyInstance();
  }

  public void execute(FunctionContext context) {
    Object[] arguments = (Object[]) context.getArguments();
    String regionNamesArg = (String) arguments[0];
    String[] regionNames = regionNamesArg.split(",");
    boolean simulate = (Boolean) arguments[1];
    this.cache.getLogger().info("Executing function=" + getId() + "; regionNames=" + Arrays.toString(regionNames) + "; simulate=" + simulate);

    // Get all PdxTypes
    Map<Integer,PdxType> allPdxTypesCopy = getAllPdxTypes();
    int numExisting = allPdxTypesCopy.size();
    dumpPdxTypes(allPdxTypesCopy, "existing");

    // Iterate region values as PdxInstances and remove in use PdxTypes from the map
    for (String regionName : regionNames) {
      Region region = this.cache.getRegion(regionName);
      this.cache.getLogger().info("Processing region=" + region.getFullPath() + "; size=" + region.size());
      for (Object value : region.values()) {
        PdxInstanceImpl pdxInstance = (PdxInstanceImpl) value;
        removeInUsePdxTypes(allPdxTypesCopy, null, null, pdxInstance);
      }
    }
    int numUnused = allPdxTypesCopy.size();
    int numInUse = numExisting - numUnused;

    // Remove in use PdxTypes
    dumpPdxTypes(allPdxTypesCopy, "unused");
    if (!simulate) {
      this.cache.getRegion(PeerTypeRegistration.REGION_NAME).removeAll(allPdxTypesCopy.keySet());
      dumpPdxTypes(getAllPdxTypes(), "in use");
    }
    context.getResultSender().lastResult(new PdxTypeStatus(numExisting, numInUse, numUnused));
  }

  private void removeInUsePdxTypes(Map<Integer,PdxType> allPdxTypesCopy, Object parent, String objFieldName, Object obj) {
    if (this.cache.getLogger().fineEnabled()) {
      this.cache.getLogger().fine("Checking object with parent=" + parent + "; parentClass=" + (parent == null ? "null" : parent.getClass().getSimpleName()) + "; objectFieldName=" + objFieldName + "; object=" + obj + "; objectClass=" + obj.getClass());
    }
    if (obj instanceof PdxInstanceImpl) {
      PdxInstanceImpl pdxInstance = (PdxInstanceImpl) obj;
      PdxType pdxType = pdxInstance.getPdxType();
      allPdxTypesCopy.remove(pdxType.getTypeId());
      for (PdxField field : pdxType.getFields()) {
        String fieldName = field.getFieldName();
        Object fieldValue = pdxInstance.readField(fieldName);
        removeInUsePdxTypes(allPdxTypesCopy, obj, fieldName, fieldValue);
      }
    } else if (obj instanceof Collection) {
      ((List) obj).forEach(value -> removeInUsePdxTypes(allPdxTypesCopy, obj, objFieldName, value));
    } else if (obj instanceof Map) {
      ((Map) obj).forEach((key, value) -> {
        removeInUsePdxTypes(allPdxTypesCopy, obj, objFieldName, key);
        removeInUsePdxTypes(allPdxTypesCopy, obj, objFieldName, value);
      });
    } else {
      if (!shouldSkipRecursing(obj)) {
        this.cache.getLogger().warning("Skipping unknown object with parent=" + parent + "; parentClass=" + (parent == null ? "null" : parent.getClass().getSimpleName()) + "; objectFieldName=" + objFieldName + "; object=" + obj + "; objectClass=" + obj.getClass());
      }
    }
  }

  private Map getAllPdxTypes() {
    return this.cache.getRegion(PeerTypeRegistration.REGION_NAME).entrySet()
      .stream()
      .filter(entry -> entry.getValue() instanceof PdxType)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private void dumpPdxTypes(Map<Integer,PdxType> pdxTypes, String message) {
    StringBuilder builder = new StringBuilder();
    builder.append("The cache contains ").append(pdxTypes.size()).append(" ").append(message).append(" PdxTypes:");
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
  }

  private boolean shouldSkipRecursing(Object obj) {
    return obj == null
      || obj.getClass().isPrimitive()
      || obj instanceof Boolean
      || obj instanceof String
      || obj instanceof Number;
  }

  public String getId() {
    return getClass().getSimpleName();
  }
}
