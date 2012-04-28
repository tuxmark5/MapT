package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.Feature;

public class OpExtractMountain implements Operator<Feature, Mountain>
{
  @Override
  public Mountain apply(Feature feature)
  {
    Mountain mountain = new Mountain();
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
    
    mountain.id     = feature.getIdentifier();
    mountain.point  = geometry.getInteriorPoint();
    return mountain;
  }
}
