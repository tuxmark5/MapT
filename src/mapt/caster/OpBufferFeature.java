package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.util.List;
import org.geotools.filter.function.StaticGeometry;
import org.opengis.feature.Feature;

public class OpBufferFeature implements Operator<Feature, List<Geometry>>
{
  private double amount;
  
  public OpBufferFeature(double amount)
  {
    this.amount = amount;
  }
  
  @Override
  public void apply(Feature feature, List<Geometry> dst)
  {
    Mountain mountain = new Mountain();
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
   
    dst.add(StaticGeometry.buffer(geometry, amount));
  }
}
