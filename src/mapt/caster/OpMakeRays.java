package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class OpMakeRays implements Operator<Mountain, Object>
{
  public int              numRays;
  public double           step;
  public GeometryFactory  geomFactory;
  public double           length;
  
  public OpMakeRays(int numRays, double length)
  {
    this.numRays        = numRays;
    this.length         = length;
    this.step           = Math.PI * 2.0 / (double) numRays;
    this.geomFactory    = new GeometryFactory();
  }
  
  @Override
  public Object apply(Mountain mountain)
  {
    double      angle   = 0.0;
    RayPoint    coordA  = new RayPoint(RayPoint.Type.START, mountain.point.getCoordinate());
    RayPoint    coordB; 
    LineString  ray;
    
    coordA.z            = (Double) mountain.point.getUserData();
    mountain.rays       = new Ray[numRays];
    
    for (int i = 0; i < numRays; i++)
    {
      coordB            = new RayPoint(RayPoint.Type.END);
      coordB.x          = coordA.x + Math.cos(angle) * length;
      coordB.y          = coordA.y + Math.sin(angle) * length;
      ray               = geomFactory.createLineString(new Coordinate[] {coordA, coordB});
      mountain.rays[i]  = new Ray(coordA, coordB, ray);
      angle            += step;
    }
    
    return null;
  }
}
