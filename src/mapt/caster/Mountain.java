package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.opengis.filter.identity.FeatureId;

public class Mountain
{
  public static class RayNumberComparator implements Comparator<Mountain>
  {
    @Override
    public int compare(Mountain m1, Mountain m2)
    {
      return m2.numValidRays - m1.numValidRays;
    }
  }
  
  public FeatureId      id;
  public Point          point;
  
  public boolean        validPopulationDensity    = true;
  public boolean        validSettlementDistance   = true;
  public boolean        validHydroProximity       = true;
  public boolean        validHydroVisibility      = true;
  
  public double         populationDensity         = 0.0;
  public List<Geometry> geometries                = new ArrayList<Geometry>();
  public Ray[]          rays;
  public int            numValidRays              = 0;
  
  public Mountain()
  {
  }
  
  public void compileRays(double forestHeight)
  {
    for (Ray r: rays)
      if (r.compile(forestHeight))
        numValidRays++;
    validHydroVisibility = numValidRays > 0; 
  }
  
  public boolean isValid()
  {
    return validPopulationDensity 
        && validSettlementDistance 
        && validHydroProximity
        && validHydroVisibility;
  }
}
