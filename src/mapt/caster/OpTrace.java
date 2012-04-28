package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;

public abstract class OpTrace implements Operator<Mountain, Object>
{
  @Override
  public Object apply(Mountain m)
  {
    for (Geometry g: m.geometries)
    {
      for (Ray r: m.rays)
      {
        if (!r.isValid())
          continue;
        
        Geometry i = g.intersection(r.ray);
        if (!i.isEmpty())
          applyIntersection(m, g, r, i);
      }
    }
    
    // Invalidate rays that don't hit water
    for (Ray r: m.rays)
      if (!r.hasPointOfType(RayPoint.Type.WATER))
        r.invalidate();
    
    // Hydros are no longer needed
    m.geometries.clear();
    
    return null;
  }
  
  public abstract void applyIntersection(Mountain m, Geometry g, Ray r, Geometry i);
}
