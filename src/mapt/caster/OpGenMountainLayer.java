package mapt.caster;

import com.vividsolutions.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

public class OpGenMountainLayer extends OpGenLayer<Mountain, Object>
{
  public OpGenMountainLayer()
  {
    featureTypeBegin("mountain_p", Point.class);
    featureTypeBuilder.add("validPD", Boolean.class);
    featureTypeBuilder.add("validSD", Boolean.class);
    featureTypeBuilder.add("validHP", Boolean.class);
    featureTypeBuilder.add("validHV", Boolean.class);
    featureTypeBuilder.add("numRays", Integer.class);
    featureTypeEnd();
  }
   
  @Override
  public Object apply(Mountain m)
  {
    SimpleFeature feature = buildFeature();

    feature.setDefaultGeometry(m.point);
    feature.setAttribute(1, m.validPopulationDensity);
    feature.setAttribute(2, m.validSettlementDistance);
    feature.setAttribute(3, m.validHydroProximity);
    feature.setAttribute(4, m.validHydroVisibility);
    feature.setAttribute(5, m.numValidRays);
    
    collection.add(feature);
    return null;
  }
}
