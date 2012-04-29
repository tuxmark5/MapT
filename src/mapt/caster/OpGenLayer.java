package mapt.caster;

import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.data.collection.CollectionFeatureSource;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.FactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public abstract class OpGenLayer<A, B> implements Operator<A, B>
{
  protected SimpleFeatureCollection     collection;
  protected SimpleFeatureBuilder        featureBuilder;
  protected int                         featureId;
  protected SimpleFeatureType           featureType;
  protected SimpleFeatureTypeBuilder    featureTypeBuilder;
  protected GeometryFactory             geometryFactory;
  
  public OpGenLayer()
  {
    featureTypeBuilder  = new SimpleFeatureTypeBuilder();
    featureId           = 0;
    geometryFactory     = FactoryFinder.getGeometryFactory(null);
  }
  
  public SimpleFeature buildFeature()
  {
    return featureBuilder.buildFeature("f" + featureId++);
  }
  
  protected void featureTypeBegin(String name, Class type)
  {
    featureTypeBuilder.setName(name);
    featureTypeBuilder.setNamespaceURI("temp");
    featureTypeBuilder.add("the_geom", type);
  }
    
  protected void featureTypeEnd()
  {
    featureTypeBuilder.setDefaultGeometry("the_geom");
    featureType         = featureTypeBuilder.buildFeatureType();
    featureBuilder      = new SimpleFeatureBuilder(featureType);
    collection          = new SpatialIndexFeatureCollection(featureType);
  }
  
  protected void genEnd()
  {
  }
  
  public SimpleFeatureSource genSource()
  {
    genEnd();
    return new CollectionFeatureSource(collection);
  }
}
