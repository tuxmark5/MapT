package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;

public class OpExtractGeometry implements Operator<Feature, Geometry>
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
  public Geometry apply(Feature feature)
  {
    Geometry geom = (Geometry) feature.getDefaultGeometryProperty().getValue();
    
    if (prop != null)
    {
      Attribute attr = (Attribute) feature.getProperty(prop);

      geom.setUserData(attr.getValue());
    }
    
    return geom;
  }
}
