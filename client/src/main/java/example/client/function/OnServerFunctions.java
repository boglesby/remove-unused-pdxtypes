package example.client.function;

import example.PdxTypeStatus;
import org.springframework.data.gemfire.function.annotation.FunctionId;
import org.springframework.data.gemfire.function.annotation.OnServer;

@OnServer
public interface OnServerFunctions {

  @FunctionId("DumpPdxTypesFunction")
  PdxTypeStatus dumpPdxTypes();

  @FunctionId("RemoveUnusedPdxTypesFunction")
  PdxTypeStatus removeUnusedPdxTypes(String regionNames, boolean simulate);
}
