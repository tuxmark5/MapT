package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import java.util.ArrayList;
import java.util.List;

public class Ray
{
  public LineString       ray;
  public List<RayPoint>   points;
  public boolean          valid;
  
  public Ray(Coordinate a, Coordinate b, LineString ray)
  {
    this.ray    = ray;
    this.points = new ArrayList<RayPoint>();
    this.points.add(new RayPoint(RayPoint.Type.START, a));
    this.points.add(new RayPoint(RayPoint.Type.END,   b));
    this.valid  = true;
  }
  
  public boolean hasPointOfType(RayPoint.Type type)
  {
    for (RayPoint p: points)
      if (p.type == type)
        return true;
    return false;
  }
  
  public void invalidate()
  {
    valid = false;
  }
  
  public boolean isValid()
  {
    return valid;
  }
}
