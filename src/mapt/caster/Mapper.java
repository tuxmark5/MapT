package mapt.caster;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
        op.apply(m);
    }
  }
  
  public static <A, B> void map(List<B> list, Collection<A> collection, Operator<A, B> op)
  {
    Iterator<A> iter = collection.iterator();
    
    while (iter.hasNext())
    {
      B r = op.apply(iter.next());
      if (r != null && list != null)
        list.add(r);
    }
  }
  
  public static <B> void map(List<B> list, SimpleFeatureCollection collection, Operator<Feature, B> op)
  {
    SimpleFeatureIterator iter = collection.features();
    
    try
    {
      while (iter.hasNext())
      {
        B r = op.apply(iter.next());
        if (r != null && list != null)
          list.add(r);
      }
    }
    finally
    {
      iter.close();
    }
  }
}
