package mapt.caster;

import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

public class OpGenRayPointLayer extends OpGenLayer<Mountain, Object>
{
  public OpGenRayPointLayer()
  {
    featureTypeBegin("rayPoint", Point.class);
    featureTypeBuilder.add("type",      String.class);
    featureTypeBuilder.add("distance",  Double.class);
    featureTypeBuilder.add("z",         Double.class);
    featureTypeBuilder.add("slope",     Double.class);
    featureTypeBuilder.add("maxSlope",  Double.class);
    featureTypeBuilder.add("valid",     Integer.class);
    featureTypeEnd();
  }
   
  @Override
  public void apply(Mountain m, Object unused)
  {
    if (m.rays == null)
      return;
    
    for (Ray r: m.rays)
    {
      for (RayPoint p: r.points)
      {
        if (p.valid == 0xFFFF)
          continue;
        makeFeature(p);
        p.valid = 0xFFFF;
      }
    }
  }
  
  public static String getTypeName(RayPoint.Type type)
  {
    switch (type)
    {
      case START:         return "0";
      case END:           return "1";
      case ELEV:          return "e";
      case WATER:         return "w";
      case FOREST_BEGIN:  return "f0";
      case FOREST_END:    return "f1";
    }
    return "?";
  }
  
  private void makeFeature(RayPoint p)
  {
    SimpleFeature feature = buildFeature();
    Point         point   = geometryFactory.createPoint(p);

    feature.setDefaultGeometry(point);
    feature.setAttribute(1, getTypeName(p.type));
    feature.setAttribute(2, p.distance);
    feature.setAttribute(3, p.z);
    feature.setAttribute(4, toDeg(p.slope));
    feature.setAttribute(5, toDeg(p.maxSlope));
    feature.setAttribute(6, p.valid);
    collection.add(feature);
  }
  
  private static double toDeg(double slope)
  {
    return Math.toDegrees(Math.atan(slope));
  }
}
