import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import mapt.MapFrame;
import mapt.caster.Cartographer;
import org.geotools.map.MapContent;

public class MapT
{
  private Cartographer  m_cartographer;
  private MapContent    m_mapContent;
  private MapFrame      m_mapFrame;
  
  public MapT()
  {
    m_mapContent    = new MapContent();
    m_mapFrame      = new MapFrame(m_mapContent);
    m_cartographer  = new Cartographer();
    
    try
    {
      m_cartographer.loadLayers();
      m_cartographer.run();
    }
    catch (IOException ex)
    {
      System.err.println(ex);
    }
    
    m_mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    m_mapFrame.addLayer((new File("data/lt200shp/upes.shp")).toURI());
    //m_mapFrame.addLayer(m_cartographer.sss);
    m_mapFrame.setVisible(true);
  }

  public static void main(String[] args) throws Exception
  {
    new MapT();
  }
}