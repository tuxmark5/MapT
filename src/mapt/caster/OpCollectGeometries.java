package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collection;

public class OpCollectGeometries implements Operator<Geometry, Object>
{
  private Collection<Mountain>  mountains;
  private double                horizon;
  
  public OpCollectGeometries(Collection<Mountain> mountains, double horizon)
  {
    this.mountains  = mountains;
    this.horizon    = horizon;
  }
  
  @Override
  public Object apply(Geometry geom)
  {
    // todo: PreparedGeometry, iterate over mountains, circles
    for (Mountain m: mountains)
    {
      if (!m.isValid())
        continue;
      if (m.point.isWithinDistance(geom, horizon))
        m.geometries.add(geom);
    }
    return null;
  } 
}
