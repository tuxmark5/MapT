package mapt.caster;

import java.util.Set;

public class OpGetNonElevatedPoints implements Operator<Mountain, Set<RayPoint>>
{
  @Override
  public void apply(Mountain mountain, Set<RayPoint> dst)
  {
    if (mountain.rays == null)
      return;
    
    for (Ray ray: mountain.rays)
    {
      for (RayPoint point: ray.points)
      {
        if (!point.hasNativeHeight())
          dst.add(point);
      }
    }
  }
}
