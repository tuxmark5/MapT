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
    featureTypeBuilder.add("maxZ",      Double.class);
    featureTypeBuilder.add("valid",     Integer.class);
    featureTypeEnd();
  }
   
  @Override
  public Object apply(Mountain m)
  {
    if (m.rays == null)
      return null;
    
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
    
    return null;
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
    feature.setAttribute(4, p.maxZ);
    feature.setAttribute(5, p.valid);
    collection.add(feature);
  }
}
