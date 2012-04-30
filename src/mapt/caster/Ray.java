package mapt.caster;

import com.vividsolutions.jts.geom.LineString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class Ray
{
  static SplineInterpolator interpolator = new SplineInterpolator();
  public LineString       ray;
  public List<RayPoint>   points;
  public boolean          valid;
  
  public Ray(RayPoint a, RayPoint b, LineString ray)
  {
    this.points   = new ArrayList<RayPoint>();
    this.points.add(a);
    this.points.add(b);
    this.ray      = ray;
    this.valid    = true;
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
    RayPoint  origin    = points.get(0);
    double    maxSlope  = -Math.sqrt(3.0); // -60 deg
    
    valid = false;
    
    for (int i = 1; i < points.size(); i++)
    {
      RayPoint point  = points.get(i);
      
      if (point.valid == -1)
        break;
      
      point.slope     = (point.z - origin.z) / point.distance;
      point.maxSlope  = Math.max(point.slope, maxSlope);
      
      // uzstoja horizonta
      if (point.maxSlope > 0.0)
      {
        point.valid   = -1;
        break;
      }
      
      if (point.type == RayPoint.Type.WATER)
      {
        if ((point.slope > maxSlope) && ((origin.z - point.z) > 5.0))
        {
          point.valid = 1;
          valid       = true;
        }
        else
        {
          point.valid = -1;
        }
      }
      
      maxSlope        = point.maxSlope;
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
    double[]  x;
    double[]  y;
    int       i         = 0;
    int       numPoints = 0;
    
    for (RayPoint p: points)
      if (p.hasElevation())
        numPoints++;
    
    if (numPoints < 3)
      return;
    
    x = new double[numPoints];
    y = new double[numPoints];
    
    for (RayPoint p: points)
    {
      if (p.hasElevation())
      {
        x[i] = p.distance;
        y[i] = p.z;
        i++;
      }
    }
    
    PolynomialSplineFunction f = interpolator.interpolate(x, y);
    
    for (RayPoint p: points)
      if (!p.hasElevation())
        p.z = f.value(p.distance);
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
