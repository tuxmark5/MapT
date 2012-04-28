package mapt.caster;

import java.io.IOException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.FeatureVisitor;

public abstract class TaskFeatureVisitor extends Task implements FeatureVisitor
{
  protected SimpleFeatureCollection collection;
  
  public TaskFeatureVisitor(Cartographer c, SimpleFeatureCollection collection)
  {
    super(c);
    this.collection = collection;
  }
  
  @Override
  public void run() throws IOException
  {
    SimpleFeatureIterator iter = collection.features();
    
    try
    {
      while (iter.hasNext())
      {
        visit(iter.next());
      }
    }
    finally
    {
      iter.close();
    }
  }
}
