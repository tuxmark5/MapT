package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class OpTraceForest extends OpTrace
{
  @Override
  public void applyIntersection(Mountain m, Geometry g, Ray r, Geometry intersection)
  { 
    int numGeometries = intersection.getNumGeometries();
    
    for (int i = 0; i < numGeometries; i++)
    {
      LineString  lineString  = (LineString) intersection.getGeometryN(i);
      int         numPoints   = lineString.getNumPoints();
      Coordinate  coord0      = lineString.getPointN(0).getCoordinate();
      Coordinate  coord1      = lineString.getPointN(numPoints - 1).getCoordinate();
      RayPoint    point0      = new RayPoint(RayPoint.Type.FOREST_BEGIN, coord0);
      RayPoint    point1      = new RayPoint(RayPoint.Type.FOREST_END, coord1);
      
      r.points.add(point0);
      r.points.add(point1);
    }
  }
}
