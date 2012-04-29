package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

public class OpExtractMountain implements Operator<Feature, Mountain>
{
  @Override
  public Mountain apply(Feature feature)
  {
    Mountain  mountain    = new Mountain();
    Geometry  geometry    = (Geometry) feature.getDefaultGeometryProperty().getValue();
    Property  heightProp  = feature.getProperty("AUKSTIS");
    
    mountain.id     = feature.getIdentifier();
    mountain.point  = geometry.getInteriorPoint();
    mountain.point.setUserData(heightProp.getValue());
    return mountain;
  }
}
