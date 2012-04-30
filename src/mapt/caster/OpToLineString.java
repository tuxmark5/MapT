package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.util.List;
import org.opengis.feature.Feature;

public class OpToLineString implements Operator<Feature, List<Geometry>>
{
  @Override
  public void apply(Feature feature, List<Geometry> dst)
  {
    Geometry g  = (Geometry) feature.getDefaultGeometryProperty().getValue(); 
    Geometry g2 = g.getFactory().createLineString(g.getCoordinates());
    
    dst.add(g2);
  }  
}
