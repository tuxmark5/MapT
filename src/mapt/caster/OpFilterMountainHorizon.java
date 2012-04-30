package mapt.caster;

public class OpFilterMountainHorizon implements Operator<Mountain, Object>
{
  @Override
  public void apply(Mountain m, Object unused)
  {
    if (m.geometries.isEmpty())
      m.validHydroProximity = false;
  } 
}
