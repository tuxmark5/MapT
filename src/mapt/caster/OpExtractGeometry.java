package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.util.List;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;

public class OpExtractGeometry implements Operator<Feature, List<Geometry>>
{
  public String prop;
  
  public OpExtractGeometry()
  {
  }
  
  public OpExtractGeometry(String prop)
  {
    this.prop = prop;
  }
  
  @Override
  public void apply(Feature feature, List<Geometry> dst)
  {
    Geometry geom = (Geometry) feature.getDefaultGeometryProperty().getValue();
    
    if (prop != null)
    {
      Attribute attr = (Attribute) feature.getProperty(prop);

      geom.setUserData(attr.getValue());
    }
    
    dst.add(geom);
  }
}
