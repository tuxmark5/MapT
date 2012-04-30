package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class OpQueryElevation implements Operator<RayPoint, Object>
{
  private GeometryFactory       gf;
  private FilterFactory2        ff;
  private SimpleFeatureSource   surfaceSource;
  
  public OpQueryElevation(SimpleFeatureSource surfaceSource)
  {
    this.gf             = JTSFactoryFinder.getGeometryFactory(null);
    this.ff             = CommonFactoryFinder.getFilterFactory2(null);
    this.surfaceSource  = surfaceSource;
  }
  
  @Override
  public void apply(RayPoint src, Object unused)
  {
    Geometry              geometry  = gf.createPoint(src);
    Filter                filter    = ff.contains(ff.property("the_geom"), ff.literal(geometry));
    SimpleFeatureIterator iter      = null;
      
    try
    {
      iter = surfaceSource.getFeatures(filter).features();
      
      if (iter.hasNext())
      {
        SimpleFeature feature = iter.next();
        src.z = (Double) feature.getAttribute("Aukstis");
      }
      else
      {
        //System.out.println("Can't find elevation for point");
        src.z = 10000.0;
      }
    }
    catch (IOException ex)
    {
      System.err.println(ex);
    }
    finally
    {
      if (iter != null)
        iter.close();
    }
  }
}
