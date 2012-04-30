package mapt.caster;

import java.util.Collection;
import java.util.Iterator;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.Feature;

public class Mapper
{
  public static void map(Collection<Mountain> collection, Operator<Mountain, Object> op)
  {
    Iterator<Mountain> iter = collection.iterator();
    
    while (iter.hasNext())
    {
      Mountain m = iter.next();
      if (m.isValid())
        op.apply(m, null);
    }
  }
  
  public static void mapAll(Collection<Mountain> collection, Operator<Mountain, Object> op)
  {
    Iterator<Mountain> iter = collection.iterator();
    
    while (iter.hasNext())
      op.apply(iter.next(), null);
  }
  
  public static <A, B> void map(Collection<A> collection, Operator<A, B> op, B dst)
  {
    Iterator<A> iter = collection.iterator();
    
    while (iter.hasNext())
    {
      op.apply(iter.next(), dst);
    }
  }
  
  public static <B> void map(SimpleFeatureCollection collection, Operator<Feature, B> op, B dst)
  {
    SimpleFeatureIterator iter = collection.features();
    
    try
    {
      while (iter.hasNext())
        op.apply(iter.next(), dst);
    }
    finally
    {
      iter.close();
    }
  }
}
