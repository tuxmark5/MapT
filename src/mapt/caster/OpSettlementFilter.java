package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.Feature;

public class OpSettlementFilter implements Operator<Feature, Object>
{
  private Cartographer  c;
  private double        distance;
  
  public OpSettlementFilter(Cartographer c, double distance)
  {
    this.c        = c;
    this.distance = distance;
  }
  
  @Override
  public void apply(Feature feature, Object unused)
  {
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
    
    for (Mountain m: c.mountains)
    {
      if (!m.isValid())
        continue;
      if (m.point.isWithinDistance(geometry, distance))
        m.validSettlementDistance = false;
    }
  }
}
