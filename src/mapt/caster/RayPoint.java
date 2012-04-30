package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.Comparator;

public class RayPoint extends Coordinate
{
  public static class DistanceComparator implements Comparator<RayPoint>
  {
    @Override
    public int compare(RayPoint a, RayPoint b)
    {
      if (a.type == Type.START)
        return -1;
      return (int) Math.signum(a.distance - b.distance);
    }
  }
  
  public enum Type
  {
    START,
    END,
    FOREST_BEGIN,
    FOREST_END,
    ELEV,
    WATER
  }
  
  public Type     type;
  public double   distance;
  public double   slope;
  public double   maxSlope;
  public int      valid;
  
  public RayPoint(Type type)
  {
    this.type = type;
  }
  
  public RayPoint(Type type, Coordinate coord)
  {
    super(coord);
    this.type = type;
  }
  
  public boolean hasElevation()
  {
    return (type == Type.START) || (type == Type.END) || (type == Type.ELEV);
  }
  
  public boolean hasNativeHeight()
  {
    return (type == Type.START) || (type == Type.ELEV);
  }

  public void interpolateZ(RayPoint p0, RayPoint p1)
  {
    if (!p0.hasNativeHeight() || !p1.hasNativeHeight())
      System.err.println("NO NATIVE HEIGHT! ");
    //if (p0.distance < distance && distance < p1.distance)
    
    double d  = (p1.distance - p0.distance);
    double c0 = (   distance - p0.distance) / d;
    double c1 = (p1.distance -    distance) / d;
    
    z = c0 * p0.z + c1 * p1.z; 
  }
}
