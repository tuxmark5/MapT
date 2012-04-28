package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import org.opengis.feature.Feature;

public class TaskDistrictFilter extends TaskFeatureVisitor
{
  public TaskDistrictFilter(Cartographer c, double maxDensity) throws IOException
  {
    super(c, c.districtSource.getFeatures());
  }
  
  @Override
  public void visit(Feature feature)
  {
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty();
    
    for (Mountain m: c.mountains)
    {
      if (!m.isValid())
        continue;
        
      if (geometry.contains(m.point))
      {
        //geometry.get
        //if ( )
      }
    }
  }
}
