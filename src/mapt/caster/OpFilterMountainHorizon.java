package mapt.caster;

public class OpFilterMountainHorizon implements Operator<Mountain, Object>
{
  @Override
  public Object apply(Mountain m)
  {
    if (m.geometries.isEmpty())
      m.validHydroProximity = false;
    return null;
  } 
}
