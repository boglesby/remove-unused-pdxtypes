package example;

import java.io.Serializable;

public class PdxTypeStatus implements Serializable {

  private int numExisting;

  private int numInUse;

  private int numUnused;

  public PdxTypeStatus(int numExisting, int numInUse, int numUnused) {
    this.numExisting = numExisting;
    this.numInUse = numInUse;
    this.numUnused = numUnused;
  }

  public int getNumExisting() {
    return this.numExisting;
  }

  public int getNumInUse() {
    return this.numInUse;
  }

  public int getNumUnused() {
    return this.numUnused;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
      "numExisting=" + this.numExisting +
      ", numInUse=" + this.numInUse +
      ", numUnused=" + this.numUnused +
      '}';
  }
}
