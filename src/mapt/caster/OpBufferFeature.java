package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.filter.function.StaticGeometry;
import org.opengis.feature.Feature;

public class OpBufferFeature implements Operator<Feature, Geometry>
{
  private double amount;
  
  public OpBufferFeature(double amount)
  {
    this.amount = amount;
  }
  
  @Override
  public Geometry apply(Feature feature)
  {
    Mountain mountain = new Mountain();
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
    return StaticGeometry.buffer(geometry, amount);
  }
}
