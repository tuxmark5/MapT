package mapt;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.geotools.filter.FilterAbstract;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;

public class Intersects extends FilterAbstract
{
  private String      m_geomAttr;
  private Geometry    m_geom;
  
  public Intersects(FilterFactory factory, String geomAttr, ReferencedEnvelope env)
  {
    super(factory);
    
    LinearRing      ring;
    GeometryFactory gf      = JTSFactoryFinder.getGeometryFactory(null);
    Coordinate[]    coords  = new Coordinate[]
    {
      new Coordinate(env.getMinX(), env.getMinY()),
      new Coordinate(env.getMinX(), env.getMaxY()),
      new Coordinate(env.getMaxX(), env.getMaxY()),
      new Coordinate(env.getMaxX(), env.getMinY()),
      new Coordinate(env.getMinX(), env.getMinY())
    };
    
    ring        = gf.createLinearRing(coords);
    m_geomAttr  = geomAttr;
    m_geom      = gf.createPolygon(ring, null);
  }
  
  @Override
  public boolean evaluate(SimpleFeature o)
  {
    Geometry geometry = (Geometry) o.getAttribute(m_geomAttr);
   
    return geometry.intersects(m_geom);
  }

  @Override
  public boolean evaluate(Object o)
  {
    return evaluate((SimpleFeature) o);
  }
}
