package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;

public abstract class OpTrace implements Operator<Mountain, Object>
{
  @Override
  public void apply(Mountain m, Object unused)
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
    
    // Hydros are no longer needed
    m.geometries.clear();
  }
  
  public abstract void applyIntersection(Mountain m, Geometry g, Ray r, Geometry i);
}
