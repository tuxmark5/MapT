package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.util.ArrayList;
import java.util.List;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

public class OpGenSettlementLayer extends OpGenLayer<Feature, Object>
{
  public List<Geometry>   geometries;
  public double           minSettlementDistance;
  
  public OpGenSettlementLayer(Cartographer c)
  {
    this.geometries             = new ArrayList<Geometry>();
    this.minSettlementDistance  = c.minSettlementDistance;
    
    featureTypeBegin("settlement2", MultiPolygon.class);
    featureTypeEnd();
  }
  
  @Override
  public Object apply(Feature feature)
  {
    geometries.add((Geometry) feature.getDefaultGeometryProperty().getValue());
    
    return null;
  }
  
  @Override
  protected void genEnd()
  {
    SimpleFeature feature       = buildFeature();
    Geometry      multiGeometry = geometryFactory.buildGeometry(geometries);
        
    feature.setDefaultGeometry(multiGeometry.buffer(minSettlementDistance));
    collection.add(feature);
  }
}
