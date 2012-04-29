import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import mapt.MapFrame;
import mapt.caster.Cartographer;
import mapt.caster.StyleGenerator;
import org.geotools.map.MapContent;

public class MapT
{
  private Cartographer    m_cartographer;
  private MapContent      m_mapContent;
  private MapFrame        m_mapFrame;
  
  public MapT()
  {
    m_mapContent    = new MapContent();
    m_cartographer  = new Cartographer();
    m_mapFrame      = new MapFrame(m_cartographer, m_mapContent);
    
    try
    {
      m_cartographer.loadLayers();
      //m_cartographer.run();
    }
    catch (IOException ex)
    {
      System.err.println(ex);
    }
    
    m_mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //m_mapFrame.addLayer((new File("data/lt200shp/upes.shp")).toURI());
    
    m_mapFrame.addLayer(StyleGenerator.Type.DISTRICT,   m_cartographer.districtSource).setVisible(false);
    m_mapFrame.addLayer(StyleGenerator.Type.FOREST,     m_cartographer.forestSource).setVisible(false);
    m_mapFrame.addLayer(StyleGenerator.Type.LAKE,       m_cartographer.lakeSource).setVisible(true);
    m_mapFrame.addLayer(StyleGenerator.Type.MOUNTAIN,   m_cartographer.mountainSource).setVisible(false);
    m_mapFrame.addLayer(StyleGenerator.Type.RIVER,      m_cartographer.riverSource).setVisible(true);
    m_mapFrame.addLayer(StyleGenerator.Type.SETTLEMENT, m_cartographer.settlementSource).setVisible(false);
    m_mapFrame.addLayer(StyleGenerator.Type.SURFACE,    m_cartographer.surfaceSource).setVisible(false);
    
    m_mapFrame.setVisible(true);
  }

  public static void main(String[] args) throws Exception
  {
    new MapT();
  }
}