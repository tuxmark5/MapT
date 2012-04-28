package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import org.opengis.feature.Feature;

public class TaskExtractMountains extends TaskFeatureVisitor
{
  public TaskExtractMountains(Cartographer c) throws IOException
  {
    super(c, c.mountainSource.getFeatures());
  }
  
  @Override
  public void visit(Feature feature)
  {
    Mountain mountain = new Mountain();
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
    
    mountain.id     = feature.getIdentifier();
    mountain.point  = geometry.getInteriorPoint();
    
    c.mountains.add(mountain);
  }
}
