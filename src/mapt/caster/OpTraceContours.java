package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class OpTraceContours extends OpTrace
{
  @Override
  public void applyIntersection(Mountain m, Geometry g, Ray r, Geometry i)
  {
    double elev = (Integer) g.getUserData();
    
    for (Coordinate c: i.getCoordinates())
    {
      RayPoint point = new RayPoint(RayPoint.Type.ELEV, c);
      point.z = elev;
      r.points.add(point);
    }
  }
}
