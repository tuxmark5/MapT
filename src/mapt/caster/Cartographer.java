package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.geotools.data.collection.CollectionFeatureSource;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class Cartographer
{
  public List<Mountain>           mountains;
  
  public ShapefileDataStore       districtStore;
  public ShapefileDataStore       forestStore;
  public ShapefileDataStore       lakeStore;
  public ShapefileDataStore       mountainStore;  
  public ShapefileDataStore       riverStore;
  public ShapefileDataStore       settlementStore;
  public ShapefileDataStore       surfaceStore;
  
  public SimpleFeatureSource      districtSource;
  public SimpleFeatureSource      forestSource;
  public SimpleFeatureSource      lakeSource;
  public SimpleFeatureSource      mountainSource;
  public SimpleFeatureSource      riverSource;
  public SimpleFeatureSource      settlementSource;
  public SimpleFeatureSource      surfaceSource;
  
  public Cartographer()
  {
    
  }
  
  public void compileHydroLayer()
  {
    // 2 district filter
    
    // 3 settlement buffer
    // 4 settlement filter
   
    // 1 hydro layer
    // 6 mountain buffer
    // 5 candidate selection
    
    
    
    // 
  }
  
  public void loadLayers() throws IOException
  {
    mountains           = new ArrayList<Mountain>();
    
    districtStore       = loadData("data/lt250shp/AdminVien_L.shp");
    forestStore         = loadData("data/lt200shp/miskai.shp");
    lakeStore           = loadData("data/lt250shp/Ezerai.shp");
    mountainStore       = loadData("data/lt200shp/virsukal.shp");
    riverStore          = loadData("data/lt250shp/Upes_L.shp");
    settlementStore     = loadData("data/lt250shp/Vietoves_P.shp");
    surfaceStore        = loadData("data/lt50shp/elev.shp");
    
    districtSource      = districtStore.getFeatureSource();
    forestSource        = forestStore.getFeatureSource();
    lakeSource          = lakeStore.getFeatureSource();
    mountainSource      = mountainStore.getFeatureSource();
    riverSource         = riverStore.getFeatureSource();
    settlementSource    = settlementStore.getFeatureSource();
    surfaceSource       = surfaceStore.getFeatureSource();
  }
  
  
  
  
  
  public SimpleFeatureSource sss;
  
  
  public SimpleFeatureType make()
  {
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    
    builder.setName("hydro");
    builder.setNamespaceURI("temp");
    
    builder.add("the_geom", LineString.class);
    builder.setDefaultGeometry("the_geom");
    
    return builder.buildFeatureType();
  }
  
  private void processForests() throws IOException
  {
    List<Geometry> forests = new ArrayList<Geometry>();
    
    stepBegin("Extracting forest geometry");
    Mapper.map(forests,   forestSource.getFeatures(),       new OpExtractGeometry());
    
    stepBegin("Collecting forest geometries");
    Mapper.map(null,      forests,                          new OpCollectGeometries(mountains, 500.0));
    
    stepBegin("Tracing bound forest geometries");
    Mapper.map(           mountains,                        new OpTraceForest());
  }
  
  private void processHydros() throws IOException
  {
    List<Geometry> hydros = new ArrayList<Geometry>();
    
    stepBegin("Extracting river geometry");
    Mapper.map(hydros,    riverSource.getFeatures(),        new OpExtractGeometry());
    
    stepBegin("Extracting and flattening lake geometry");
    Mapper.map(hydros,    lakeSource.getFeatures(),         new OpToLineString());
    
    stepBegin("Collecting hydros");
    Mapper.map(null,      hydros,                           new OpCollectGeometries(mountains, 500.0));
    
    stepBegin("Invalidating mountains that are not close enough to hydros");
    Mapper.map(           mountains,                        new OpFilterMountainHorizon());
    
    stepBegin("Generating rays for each valid mountain");
    Mapper.map(           mountains,                        new OpMakeRays(8, 500.0));
    
    stepBegin("Tracing bound hydros geometries");
    Mapper.map(           mountains,                        new OpTraceHydro());
  }
  
  private void processSettlements() throws IOException
  {
    // Settlements
    stepBegin("Invalidating mountains that are too close to settlements");
    Mapper.map(null,      settlementSource.getFeatures(),   new OpSettlementFilter(this, 10000.0));
  }
  
  private void processSurface() throws IOException
  {
    List<Geometry> elevs = new ArrayList<Geometry>();
    
    stepBegin("Extracting surface geometry");
    Mapper.map(elevs,     surfaceSource.getFeatures(),      new OpExtractGeometry("AUKSTIS"));
    
    stepBegin("Collecting surface geometries");
    Mapper.map(null,      elevs,                            new OpCollectGeometries(mountains, 500.0));
    
    stepBegin("Tracing bound surface geometries");
    Mapper.map(           mountains,                        new OpTraceSurface());
  }
  
  
  public void run() throws IOException
  {
    // Mountains
    stepBegin("Extracting mountain data from mountain shapefile");
    Mapper.map(mountains, mountainSource.getFeatures(),     new OpExtractMountain());
    
    processSettlements();
    processHydros();
    //processSurface();
    //processForests();
    
    // Mountains/2
    
    
    
    // visual line layer: horizon circles + rays + rivers
    
    int valid = 0;
    for (Mountain m: mountains)
    {
      if (m.isValid())
        valid++;
    }
    System.out.println("D2 " + valid + " of " + mountains.size());
    

    SimpleFeatureType ftype = make();
    
    SpatialIndexFeatureCollection c = new SpatialIndexFeatureCollection(ftype);
    SimpleFeatureBuilder bb = new SimpleFeatureBuilder(ftype);
    
    /*System.out.println("D2 " + hydros.size());
    
    for (int i = 0; i < hydros.size(); i++)
    {
      //FeatureId       id    = new FeatureIdImpl("f" + i);
      SimpleFeature   feat  = bb.buildFeature("f" + i);
      
      feat.setDefaultGeometry(hydros.get(i));
      //SimpleFeature   feat  = new SimpleFeatureImpl(new Object[]{}, ftype, id, false);
      
      c.add(feat);
    }*/
    
    //sss = new SpatialIndexFeatureSource(c);
    sss = new CollectionFeatureSource(c);
    System.out.println("E " + c.size());
    
    
    //TaskExtractMountains t1 = new TaskExtractMountains(this);
    
    //t1.run();

  }
  
  public void stepBegin(String name)
  {
    System.out.println("> " + name);
  }
  
  public static ShapefileDataStore loadData(String relativePath) 
  {
    try
    {
      URI uri = (new File(relativePath)).toURI();
      return new IndexedShapefileDataStore(uri.toURL(), true, true);
    }
    catch (MalformedURLException ex)
    {
      return null;
    }
  }
}
