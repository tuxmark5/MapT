package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.util.ArrayList;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.function.StaticGeometry;
import org.opengis.feature.Feature;

public class TaskBuffer extends TaskFeatureVisitor
{
  private double              amount;
  public  ArrayList<Geometry> geometries  = new ArrayList<Geometry>();
  
  public TaskBuffer(Cartographer c, SimpleFeatureCollection collection, double amount) throws IOException
  {
    super(c, collection);
    this.amount = amount;
  }
  
  @Override
  public void visit(Feature feature)
  {
    Geometry geometry = (Geometry) feature.getDefaultGeometryProperty();
    
    geometries.add(StaticGeometry.buffer(geometry, amount));
  }
}
