package mapt.caster;

import com.vividsolutions.jts.geom.LineString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ray
{
  public LineString       ray;
  public List<RayPoint>   points;
  public RayPoint         target;
  public boolean          valid;
  
  public Ray(RayPoint a, RayPoint b, LineString ray)
  {
    this.points = new ArrayList<RayPoint>();
    this.points.add(a);
    this.points.add(b);
    this.ray    = ray;
    this.valid  = true;
  }
  
  private void applyForestModifiers(double forestHeight)
  {
    boolean forest = false;
    
    for (RayPoint p: points)
    {
      /**/ if (p.type == RayPoint.Type.FOREST_BEGIN)
      { p.z += forestHeight; forest = true; }
      else if (p.type == RayPoint.Type.FOREST_END)
      { p.z += forestHeight; forest = false; }
      else if (p.type != RayPoint.Type.WATER && forest) // upe miske
      { p.z += forestHeight; }
    }
  }
  
  private void checkVisibility()
  {
    RayPoint  p0    = points.get(0);
    RayPoint  p1    = target;
    double    slope = (p1.z - p0.z) / p1.distance;
    
    for (RayPoint p: points)
    {
      p.maxZ  = p0.z + p.distance * slope;
      p.valid = p.z > p.maxZ ? -1 : 1;
      
      if (p.valid == -1)
      {
        valid = false;
        break;
      }
      
      if (p.type == RayPoint.Type.WATER)
        break;
    }
  }
   
  public boolean compile(double forestHeight)
  {
    if (valid) sortByDistance();
    if (valid) interpolateZ();
    if (valid) applyForestModifiers(forestHeight);
    if (valid) checkVisibility();
    return valid;
  }
  
  public RayPoint getNextElevPoint(int i)
  {
    for (; i < points.size(); i++)
    {
      RayPoint point = points.get(i);
      if (point.type == RayPoint.Type.ELEV)
        return point;
    }
    return null;
  }
  
  public boolean hasPointOfType(RayPoint.Type type)
  {
    for (RayPoint p: points)
      if (p.type == type)
        return true;
    return false;
  }
  
  private void interpolateZ()
  {
    RayPoint point0 = points.get(0);
    RayPoint point1;
    
    for (int i = 1; i < points.size(); i++)
    {
      RayPoint point = points.get(i);
     
      if (point.type == RayPoint.Type.ELEV)
      {
        point0 = point;
      }
      else
      {
        point1 = getNextElevPoint(i);
        if (point1 != null)
        {
          point.interpolateZ(point0, point1);
        }
        else
        {
          System.err.println("Openended point");
          //valid = false;
          return;
        }
      }
    }
  }
  
  public void invalidate()
  {
    valid = false;
  }
  
  public boolean isValid()
  {
    return valid;
  }
  
  private void sortByDistance()
  {
    RayPoint origin = points.get(0);
    
    for (int i = 1; i < points.size(); i++)
    {
      RayPoint point = points.get(i);
      point.distance = point.distance(origin);
    }
    
    Collections.sort(points, new RayPoint.DistanceComparator());
  }
}
