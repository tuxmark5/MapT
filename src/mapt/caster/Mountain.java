package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import org.opengis.filter.identity.FeatureId;

public class Mountain
{
  public FeatureId      id;
  public Point          point;
  
  public boolean        validPopulationDensity    = true;
  public boolean        validSettlementDistance   = true;
  public boolean        validHydroProximity       = true;
  
  public double         populationDensity         = 0.0;
  public List<Geometry> geometries                = new ArrayList<Geometry>();
  public Ray[]          rays;
  
  public Mountain()
  {
    
  }
  
  public boolean isValid()
  {
    return validPopulationDensity && validSettlementDistance && validHydroProximity;
  }
}
