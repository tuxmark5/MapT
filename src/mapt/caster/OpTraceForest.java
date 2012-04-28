package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class OpTraceForest extends OpTrace
{
  @Override
  public void applyIntersection(Mountain m, Geometry g, Ray r, Geometry i)
  {  
    System.out.println("STUFF " + i.getGeometryType());
    
    /*for (Coordinate c: i.getCoordinates())
    {
      RayPoint point = new RayPoint(RayPoint.Type.ELEV, c);
      point.z = elev;
      r.points.add(point);
    }*/
  }
}
