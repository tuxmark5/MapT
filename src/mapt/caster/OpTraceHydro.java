package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class OpTraceHydro extends OpTrace
{
  @Override
  public void apply(Mountain m, Object unused)
  {
    super.apply(m, unused);
    
    // Invalidate rays that don't hit water
    for (Ray r: m.rays)
      if (!r.hasPointOfType(RayPoint.Type.WATER))
        r.invalidate();
  }
  
  @Override
  public void applyIntersection(Mountain m, Geometry g, Ray r, Geometry i)
  {
    for (Coordinate point: i.getCoordinates())
    {
      RayPoint target = new RayPoint(RayPoint.Type.WATER, point);
      r.points.add(target);
    }
  }
}
