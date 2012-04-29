package mapt;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;
import org.geotools.data.FeatureSource;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Util
{
  public static void dispose(Layer layer)
  {
    try
    {
      FeatureSource<?, ?>       source      = layer.getFeatureSource();
      FeatureCollection<?, ?>   collection  = source.getFeatures();
      
      if (collection instanceof SpatialIndexFeatureCollection)
        ((SpatialIndexFeatureCollection) collection).clear();
      layer.dispose();
    }
    catch (IOException ex)
    {
      System.out.println(ex.toString());
    }
  }
  
  public static Rectangle toRectangle(Point p0, Point p1)
  {
    int x = Math.min(p0.x,  p1.x);
    int y = Math.min(p0.y,  p1.y);
    int w = Math.abs(p0.x - p1.x);
    int h = Math.abs(p0.y - p1.y);
    return new Rectangle(x, y, w, h);
  }

  public static void inverseTransform(AffineTransform t, Rectangle.Double r)
  {
    Point.Double p0 = new Point.Double(r.x, r.y);
    Point.Double p1 = new Point.Double(r.x + r.width, r.y + r.height);
    
    try
    {
      t.inverseTransform(p0, p0);
      t.inverseTransform(p1, p1);
      r.x       = p0.x;
      r.y       = p0.y;
      r.width   = p1.x - p0.x;
      r.height  = p1.y - p0.y;
    }
    catch (NoninvertibleTransformException ex)
    {
    }
  }
  
  public static Rectangle.Double mapToView(AffineTransform t, ReferencedEnvelope env)
  {
    Point.Double p0 = new Point.Double(env.getMinX(), env.getMinY());
    Point.Double p1 = new Point.Double(env.getMaxX(), env.getMaxY());
   
    try
    {
      t.inverseTransform(p0, p0);
      t.inverseTransform(p1, p1);
    }
    catch (NoninvertibleTransformException ex)
    {
    }
    
    return new Rectangle.Double(p0.x, p1.y, p1.x - p0.x, p0.y - p1.y);
  }
  
  public static ReferencedEnvelope viewToMap(CoordinateReferenceSystem crs, AffineTransform t, 
    Rectangle.Double r)
  {
    Point.Double        p0  = new Point.Double(r.x, r.y);
    Point.Double        p1  = new Point.Double(r.x + r.width, r.y + r.height);
    ReferencedEnvelope  env = new ReferencedEnvelope(crs);
    
    t.transform(p0, p0);
    t.transform(p1, p1);
    
    env.init(p0.x, p1.x, p0.y, p1.y);
    return env;
  }
}
