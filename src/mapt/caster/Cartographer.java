package mapt.caster;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;

public class Cartographer implements Runnable
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
  
  public SimpleFeatureSource      raySource;
  public SimpleFeatureSource      rayPointSource;
  public SimpleFeatureSource      mountain2Source;
  public SimpleFeatureSource      settlement2Source;
  
  public double                   forestHeight          = 20.0;
  public double                   horizonRadius         = 500.0;
  public double                   minSettlementDistance = 10000.0;
  public int                      rayCount              = 8;
  public double                   rayLength             = 500.0;
  
  public ProgressNotifier         progressNotifier      = new ProgressNotifier();
  
  public Cartographer()
  {
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
    Mapper.map(null, settlementSource.getFeatures(), op);
    
    return op.genSource();
  }
  
  public void loadLayers() throws IOException
  {
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
  
  private void processForests() throws IOException
  {
    List<Geometry> forests = new ArrayList<Geometry>();
    
    stepBegin("Extracting forest geometry");
    Mapper.map(forests,   forestSource.getFeatures(),       new OpExtractGeometry());
    
    stepBegin("Collecting forest geometries");
    Mapper.map(null,      forests,                          new OpCollectGeometries(mountains, horizonRadius));
    
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
    Mapper.map(null,      hydros,                           new OpCollectGeometries(mountains, horizonRadius));
    
    stepBegin("Invalidating mountains that are not close enough to hydros");
    Mapper.map(           mountains,                        new OpFilterMountainHorizon());
    
    stepBegin("Generating rays for each valid mountain");
    Mapper.map(           mountains,                        new OpMakeRays(rayCount, rayLength));
    
    stepBegin("Tracing bound hydros geometries");
    Mapper.map(           mountains,                        new OpTraceHydro());
  }
  
  private void processSettlements() throws IOException
  {
    // Settlements
    stepBegin("Invalidating mountains that are too close to settlements");
    Mapper.map(null,      settlementSource.getFeatures(),   new OpSettlementFilter(this, minSettlementDistance));
  }
  
  private void processSurface() throws IOException
  {
    List<Geometry> elevs = new ArrayList<Geometry>();
    
    stepBegin("Extracting surface geometry");
    Mapper.map(elevs,     surfaceSource.getFeatures(),      new OpExtractGeometry("AUKSTIS"));
    
    stepBegin("Collecting surface geometries");
    Mapper.map(null,      elevs,                            new OpCollectGeometries(mountains, horizonRadius));
    
    stepBegin("Tracing bound surface geometries");
    Mapper.map(           mountains,                        new OpTraceSurface());
  }
  
  @Override
  public void run()
  {
    try
    {
      progressNotifier.taskBegin(20);
      
      // Mountains
      mountains           = new ArrayList<Mountain>();
      
      stepBegin("Extracting mountain data from mountain shapefile");
      Mapper.map(mountains, mountainSource.getFeatures(),     new OpExtractMountain());

      processSettlements();
      processHydros();
      processSurface();
      processForests();

      stepBegin("Compiling mountain rays");
      Mapper.map(           mountains,                        new OpCompileRays(forestHeight));

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
      URI uri = (new File(relativePath)).toURI();
      return new IndexedShapefileDataStore(uri.toURL(), true, false);
    }
    catch (MalformedURLException ex)
    {
      return null;
    }
  }
}
