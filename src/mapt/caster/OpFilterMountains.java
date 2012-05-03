package mapt.caster;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Location;
import java.util.List;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;

public class OpFilterMountains implements Operator<Feature, Object>
{
  public Filter           filter;
  public List<Mountain>   mountains;
  
  public OpFilterMountains(List<Mountain> mountains, String filterStr) throws CQLException
  {
    this.filter     = CQL.toFilter(filterStr);
    this.mountains  = mountains; 
  }
  
  @Override
  public void apply(Feature feature, Object unused)
  {
    Geometry                  geom    = (Geometry) feature.getDefaultGeometryProperty().getValue();
    IndexedPointInAreaLocator locator = new IndexedPointInAreaLocator(geom);
    
    for (Mountain mountain: mountains)
    {
      if (!mountain.isValid())
        continue;
      
      if (locator.locate(mountain.point.getCoordinate()) == Location.INTERIOR)
      {
        if (!filter.evaluate(feature))
        {
          mountain.validPopulationDensity = false;
        }
      }
    }
  }
}
