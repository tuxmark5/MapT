package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.Feature;

public class OpToLineString implements Operator<Feature, Geometry>
{
  @Override
  public Geometry apply(Feature feature)
  {
    Geometry g = (Geometry) feature.getDefaultGeometryProperty().getValue(); 
    return g.getFactory().createLineString(g.getCoordinates());
  }  
}
