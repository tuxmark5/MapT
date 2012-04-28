package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;

public class OpTraceHydro extends OpTrace
{
  @Override
  public Object apply(Mountain m)
  {
    super.apply(m);
    
    // Invalidate rays that don't hit water
    for (Ray r: m.rays)
      if (!r.hasPointOfType(RayPoint.Type.WATER))
        r.invalidate();
    
    return null;
  }
  
  @Override
  public void applyIntersection(Mountain m, Geometry g, Ray r, Geometry i)
  {
    r.points.add(new RayPoint(RayPoint.Type.WATER, i.getCoordinate()));
  }
}
