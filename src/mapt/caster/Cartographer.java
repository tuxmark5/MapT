package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.FilterFactory2;

public class Cartographer implements Runnable
{
  public FilterFactory2           ff;
  public List<Mountain>           mountains;
  
  public ShapefileDataStore       contourStore;
  public ShapefileDataStore       districtStore;
  public ShapefileDataStore       forestStore;
  public ShapefileDataStore       lakeStore;
  public ShapefileDataStore       mountainStore;  
  public ShapefileDataStore       riverStore;
  public ShapefileDataStore       settlementStore;
  public ShapefileDataStore       surfaceStore;
  
  public SimpleFeatureSource      contourSource;
  public SimpleFeatureSource      districtSource;
  public SimpleFeatureSource      forestSource;
  public SimpleFeatureSource      lakeSource;
  public SimpleFeatureSource      mountainSource;
  public SimpleFeatureSource      riverSource;
  public SimpleFeatureSource      settlementSource;
  public SimpleFeatureSource      surfaceSource;
  
  public SimpleFeatureSource      raySource;
  public SimpleFeatureSource      rayPointSource;
  public SimpleFeatureSource      mountain2Source;
  public SimpleFeatureSource      settlement2Source;
  
  public ReferencedEnvelope       searchArea;
  public double                   forestHeight          = 20.0;
  public double                   horizonRadius         = 500.0;
  public double                   maxPopulationDensity  = 1000.0;
  public double                   minHeightDifference   = 10.0;
  public double                   minSettlementDistance = 10000.0;
  public int                      rayCount              = 8;
  public double                   rayLength             = 500.0;
  
  public ProgressNotifier         progressNotifier      = new ProgressNotifier();
  
  public Cartographer()
  {
    ff = CommonFactoryFinder.getFilterFactory2(null);
  }
  
  public void cleanup()
  {
    mountains           = null;
    raySource           = null;
    rayPointSource      = null;
    mountain2Source     = null;
    settlement2Source   = null;
    System.gc();
  }
  
  private SimpleFeatureCollection getFeatures(SimpleFeatureSource source) throws IOException
  {
    if (searchArea == null)
      return source.getFeatures();
    return source.getFeatures(ff.intersects(ff.property("the_geom"), ff.literal(searchArea)));
  }
  
  public SimpleFeatureSource genMountainLayer()
  {
    OpGenMountainLayer op = new OpGenMountainLayer();
    
    stepBegin("Generating mountain layer");
    Mapper.mapAll(mountains, op);
    
    return op.genSource();
  }
  
  public SimpleFeatureSource genRayLayer()
  {
    OpGenRayLayer op = new OpGenRayLayer();
    
    stepBegin("Generating ray layer");
    Mapper.mapAll(mountains, op);
    
    return op.genSource();
  }
  
  public SimpleFeatureSource genRayPointLayer()
  {
    OpGenRayPointLayer op = new OpGenRayPointLayer();
    
    stepBegin("Generating raypoint layer");
    Mapper.mapAll(mountains, op);
    
    return op.genSource();
  }
  
  public String genReport()
  {
    int           numMountains  = Math.min(10, mountains.size());
    StringBuilder report        = new StringBuilder();
    
    for (int i = 0; i < numMountains; i++)
    {
      Mountain m = mountains.get(i);
      
      report.append("<a href=\"");
      report.append(m.id.toString());
      report.append("\">");
      report.append(i + 1);
      report.append(": ");
      report.append(m.id.toString());
      report.append(" (");
      report.append(m.numValidRays);
      report.append(")</a><br/>");
    }
    return report.toString();
  }
  
  public SimpleFeatureSource genSettlementLayer() throws IOException
  {
    OpGenSettlementLayer op = new OpGenSettlementLayer(this);
    
    stepBegin("Generating settlement layer");
    Mapper.map(settlementSource.getFeatures(), op, null);
    
    return op.genSource();
  }
  
  public void loadLayers() throws IOException
  {
    contourStore        = loadData("data/lt50shp/elev.shp");
    districtStore       = loadData("data/lt200shp/rajonai.shp");
    forestStore         = loadData("data/lt200shp/miskai.shp");
    lakeStore           = loadData("data/lt250shp/Ezerai.shp");
    mountainStore       = loadData("data/lt200shp/virsukal.shp");
    riverStore          = loadData("data/lt250shp/Upes_L.shp");
    settlementStore     = loadData("data/lt250shp/Vietoves_P.shp");
    surfaceStore        = loadData("data/lt200shp/pavirs_lt_p.shp");
    
    contourSource       = contourStore.getFeatureSource();
    districtSource      = districtStore.getFeatureSource();
    forestSource        = forestStore.getFeatureSource();
    lakeSource          = lakeStore.getFeatureSource();
    mountainSource      = mountainStore.getFeatureSource();
    riverSource         = riverStore.getFeatureSource();
    settlementSource    = settlementStore.getFeatureSource();
    surfaceSource       = surfaceStore.getFeatureSource();
  }
  
  private void processContours() throws IOException
  {
    List<Geometry> contours = new ArrayList<Geometry>();
    
    stepBegin("Extracting contour geometry");
    Mapper.map(getFeatures(contourSource),      new OpExtractGeometry("AUKSTIS"), contours);
    
    stepBegin("Collecting contour geometries");
    Mapper.map(contours,                        new OpCollectGeometries(mountains, horizonRadius), null);
    
    stepBegin("Tracing bound contour geometries");
    Mapper.map(mountains,                       new OpTraceContours());
  }
  
  private void processDistricts() throws CQLException, IOException
  {
    stepBegin("Filtering mountains according to population density");
    Mapper.map(getFeatures(districtSource),     new OpFilterMountains(mountains, "PERIMETER > 200000"), null);
  }
  
  private void processForests() throws IOException
  {
    List<Geometry> forests = new ArrayList<Geometry>();
    
    stepBegin("Extracting forest geometry");
    Mapper.map(getFeatures(forestSource),       new OpExtractGeometry(), forests);
    
    stepBegin("Collecting forest geometries");
    Mapper.map(forests,                         new OpCollectGeometries(mountains, horizonRadius), null);
    
    stepBegin("Tracing bound forest geometries");
    Mapper.map(mountains,                       new OpTraceForest());
  }
  
  private void processHydros() throws IOException
  {
    List<Geometry> hydros = new ArrayList<Geometry>();
    
    stepBegin("Extracting river geometry");
    Mapper.map(getFeatures(riverSource),        new OpExtractGeometry(), hydros);
    
    stepBegin("Extracting and flattening lake geometry");
    Mapper.map(getFeatures(lakeSource),         new OpToLineString(), hydros);
    
    stepBegin("Collecting hydros");
    Mapper.map(hydros,                          new OpCollectGeometries(mountains, horizonRadius), null);
    
    stepBegin("Invalidating mountains that are not close enough to hydros");
    Mapper.map(mountains,                       new OpFilterMountainHorizon());
    
    stepBegin("Generating rays for each valid mountain");
    Mapper.map(mountains,                       new OpMakeRays(rayCount, rayLength));
    
    stepBegin("Tracing bound hydros geometries");
    Mapper.map(mountains,                       new OpTraceHydro());
  }
  
  private void processSettlements() throws IOException
  {
    // Settlements
    stepBegin("Invalidating mountains that are too close to settlements");
    Mapper.map(getFeatures(settlementSource),   new OpSettlementFilter(this, minSettlementDistance), null);
  }
  
  private void processSurface() throws IOException
  {
    Set<RayPoint> points = new HashSet<RayPoint>();
    
    stepBegin("Collecting points with unknown elevation");
    Mapper.map(mountains,                       new OpGetNonElevatedPoints(), points);
    
    stepBegin("Querying elevation for each un-elevated point");
    Mapper.map(points,                          new OpQueryElevation(surfaceSource), null);
  }
  
  @Override
  public void run()
  {
    try
    {
      progressNotifier.taskBegin(24);
      
      // Mountains
      mountains           = new ArrayList<Mountain>();
      
      stepBegin("Extracting mountain data from mountain shapefile");
      Mapper.map(getFeatures(mountainSource),   new OpExtractMountain(), mountains);

      processDistricts();
      processSettlements();
      processHydros();
      processForests();
      processSurface();
      processContours();

      stepBegin("Compiling mountain rays");
      Mapper.map(mountains,                     new OpCompileRays(forestHeight, minHeightDifference));

      raySource           = genRayLayer();
      rayPointSource      = genRayPointLayer();
      mountain2Source     = genMountainLayer();
      settlement2Source   = genSettlementLayer();
      
      stepBegin("Sorting mountains by valid ray count");
      Collections.sort(mountains, new Mountain.RayNumberComparator());
      
      stepBegin("Collecting garbage");
      System.gc();
      
      progressNotifier.taskEnd("<b>DONE:</b><br/>" + genReport());
    }
    catch (CQLException ex)
    {
      System.err.println(ex.toString());
    }
    catch (IOException ex)
    {
      System.err.println(ex.toString());
    }
  }
  
  public void stepBegin(String name)
  {
    String message = "<b>" + name + "</b><br/>";
    
    progressNotifier.stepBegin(message);
  }
  
  public void stepEnd()
  {
    progressNotifier.stepEnd();
  }
  
  public static ShapefileDataStore loadData(String relativePath) 
  {
    try
    {
      File  file  = new File(relativePath);
      Map   map   = new HashMap();
      
      map.put("create spatial index", Boolean.TRUE);
      map.put("url", file.toURI().toURL());

      return (ShapefileDataStore) DataStoreFinder.getDataStore(map);
    }
    catch (IOException ex)
    {
      return null;
    }
  }
}
