package mapt.caster;

import java.io.IOException;

public class TaskBufferSettlements extends TaskBuffer
{
  public TaskBufferSettlements(Cartographer c, double amount) throws IOException
  {
    super(c, c.settlementSource.getFeatures(), amount);
  }
}
