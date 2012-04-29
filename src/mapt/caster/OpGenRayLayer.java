package mapt.caster;

import com.vividsolutions.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;

public class OpGenRayLayer extends OpGenLayer<Mountain, Object>
{
  public OpGenRayLayer()
  {
    featureTypeBegin("ray", LineString.class);
    featureTypeBuilder.add("valid", Integer.class);
    featureTypeEnd();
  }
   
  @Override
  public Object apply(Mountain m)
  {
    if (m.rays == null)
      return null;
    
    for (Ray r: m.rays)
    {
      SimpleFeature feature = buildFeature();
      
      feature.setDefaultGeometry(r.ray);
      feature.setAttribute(1, r.valid ? 1 : 0);
      collection.add(feature);
    }
    
    return null;
  }
}
