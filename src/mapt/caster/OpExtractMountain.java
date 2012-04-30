package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.util.List;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

public class OpExtractMountain implements Operator<Feature, List<Mountain>>
{
  @Override
  public void apply(Feature feature, List<Mountain> dst)
  {
    Mountain  mountain    = new Mountain();
    Geometry  geometry    = (Geometry) feature.getDefaultGeometryProperty().getValue();
    Property  heightProp  = feature.getProperty("AUKSTIS");
    
    mountain.id     = feature.getIdentifier();
    mountain.point  = geometry.getInteriorPoint();
    mountain.point.setUserData(heightProp.getValue());
    
    dst.add(mountain);
  }
}
