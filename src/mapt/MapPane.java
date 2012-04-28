package mapt;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JComponent;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

public class MapPane extends JComponent implements MapLayerListListener, Runnable
{
  static final int TILE_BITS_W  = 8;
  static final int TILE_BITS_H  = 8;
  static final int TILE_WIDTH   = 1 << TILE_BITS_W;
  static final int TILE_HEIGHT  = 1 << TILE_BITS_H;
  static final int TILE_MASK_W  = TILE_WIDTH  - 1;
  static final int TILE_MASK_H  = TILE_HEIGHT - 1;

  private   boolean             m_initialized   = false;
  protected MapContent          m_content;
  private   GTRenderer          m_renderer;

  protected AffineTransform     m_transform     = new AffineTransform();
  protected Point.Double        m_origin        = new Point.Double();
  
  protected ReferencedEnvelope  m_lastView;
  protected ReferencedEnvelope  m_region;
  
  protected int                 m_numTilesW, m_numTilesH;
  protected Tile[][]            m_tiles;
  protected Monitor             m_renderMonitor = new Monitor();
  protected LinkedList<Tile>    m_renderQueue   = new LinkedList<Tile>();
  protected Thread              m_renderThread  = new Thread(this);
  
  public MapPane(MapContent content)
  {
    RenderingHints  javaHints;
    Map             renderHints;
    
    javaHints   = new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON);
    renderHints = new HashMap();
    renderHints.put("optimizedDataLoadingEnabled", Boolean.TRUE);
    
    m_content   = content;
    m_content.addMapLayerListListener(this);
    
    m_renderer  = new StreamingRenderer();
    m_renderer.setMapContent(m_content);
    m_renderer.setJava2DHints(javaHints);
    m_renderer.setRendererHints(renderHints);
    
    initTiles();
    m_renderThread.start();
  }
  
  protected void drawEnvelope(Graphics2D g2, ReferencedEnvelope env, Color fill, Color border)
  {
    Rectangle.Double rect = Util.mapToView(m_transform, env);
    int              x    = (int) (rect.x - m_origin.x);
    int              y    = (int) (rect.y - m_origin.y);
    
    if (fill != null)
    {
      g2.setColor(fill);
      g2.fillRect(x, y, (int) rect.width, (int) rect.height);
    }
      
    if (border != null)
    {
      g2.setColor(border);
      g2.drawRect(x, y, (int) rect.width, (int) rect.height);
    }
  }
  
  private void initTiles()
  {
    m_numTilesW = 10;
    m_numTilesH = 10;
    m_tiles     = new Tile[m_numTilesW][m_numTilesH];
    for (int y = 0; y < m_numTilesH; y++)
      for (int x = 0; x < m_numTilesW; x++)
        m_tiles[y][x] = new Tile(TILE_WIDTH, TILE_HEIGHT);
  }
  
  public MapContent getMapContent()
  {
    return m_content;
  }
  
  protected Tile getTile(int x, int y)
  {   
    int                 tx    = (x % m_numTilesW + m_numTilesW) % m_numTilesW;
    int                 ty    = (y % m_numTilesH + m_numTilesH) % m_numTilesH;
    Tile                tile  = m_tiles[ty][tx];
    Rectangle.Double    rect0 = tile.getViewRect(x, y);
    ReferencedEnvelope  env   = Util.viewToMap(m_content.getCoordinateReferenceSystem(), m_transform, rect0);

    if (!tile.m_valid || !tile.equalEnvelope(env))
    {
      tile.setEnvelope(env);
      m_renderQueue.addLast(tile);
      m_renderMonitor.notifyM();
    }
    
    return tile;
  }
  
  public void highlightLastView()
  {
    /*Timer timer = new Timer(2000, new ActionListener() 
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        m_lastView = null;
        repaint();
      }
    });
    
    timer.start();*/
  }
  
  public void invalidateCache()
  {
    for (int y = 0; y < m_numTilesH; y++)
      for (int x = 0; x < m_numTilesW; x++)
        m_tiles[y][x].m_valid = false;
    repaint();
  }
  
  @Override
  public void layerAdded(MapLayerListEvent mlle)
  {
    invalidateCache();
  }
  
  @Override
  public void layerChanged(MapLayerListEvent mlle)
  {
    System.out.println("AAX " + mlle.toString());
    invalidateCache();
  }
  
  @Override
  public void layerMoved(MapLayerListEvent mlle)
  {
    invalidateCache();
  }

  @Override
  public void layerPreDispose(MapLayerListEvent mlle)
  {
  }
  
  @Override
  public void layerRemoved(MapLayerListEvent mlle)
  {
    invalidateCache();
  }
  
  public Point.Double mapScreenToMap(Point pt)
  {
    Point.Double pt1 = new Point.Double(m_origin.x + pt.x, m_origin.y + pt.y);
    
    m_transform.transform(pt1, pt1);
    return pt1;
  }
  
  public ReferencedEnvelope mapScreenToMap(Rectangle rect)
  {
    Point.Double        p0  = new Point.Double(m_origin.x + rect.x,     m_origin.y + rect.y);
    Point.Double        p1  = new Point.Double(p0.x       + rect.width, p0.y       + rect.height);
    ReferencedEnvelope  env = new ReferencedEnvelope(m_content.getCoordinateReferenceSystem());
    
    m_transform.transform(p0, p0);
    m_transform.transform(p1, p1);
    env.init(p0.x, p1.x, p0.y, p1.y);
    
    return env;
  }
  
  @Override
  protected void paintComponent(Graphics g)
  {
    if (m_content.layers().isEmpty()) 
      return;
    
    if (!m_initialized)
    {
      m_initialized = true;
      zoomAll();
    }
    
    Graphics2D  g2          = (Graphics2D) g;
    int         numW        = Math.min(getWidth()  / TILE_WIDTH  + 2, m_numTilesW);
    int         numH        = Math.min(getHeight() / TILE_HEIGHT + 2, m_numTilesH);
    int         originX     = (int) m_origin.x;
    int         originY     = (int) m_origin.y;
    int         tileX       = (originX >> TILE_BITS_W);
    int         tileY       = (originY >> TILE_BITS_H);
    int         translateX  = -(originX & TILE_MASK_W);
    int         translateY  = -(originY & TILE_MASK_H);
    
    synchronized (m_renderQueue)
    {
      m_renderQueue.clear();
      
      for (int y = 0; y < numH; y++)
      {
        for (int x = 0; x < numW; x++)
        {
          int   x0    = translateX + x * TILE_WIDTH;
          int   y0    = translateY + y * TILE_HEIGHT;
          Image image = getTile(tileX + x, tileY + y).m_buffer;
          
          g2.drawImage(image, x0, y0, null);
        }
      }
    }
    
    if (m_lastView != null)
      drawEnvelope(g2, m_lastView, null, Color.RED);
    
    if (m_region != null)
      drawEnvelope(g2, m_region, new Color(0x00, 0xFF, 0x00, 0x20), Color.GREEN);
  }
  
  private void renderTile(Tile tile)
  {
    Image               image = tile.m_buffer;
    Graphics2D          g2    = (Graphics2D) image.getGraphics();
    Rectangle           rect  = new Rectangle(0, 0, TILE_WIDTH, TILE_HEIGHT);
    ReferencedEnvelope  env   = tile.getEnvelope();

    tile.m_valid = true;
    
    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, rect.width, rect.height);
    m_renderer.paint(g2, rect, env);//, m_transform);
  }
  
  @Override
  public void run()
  {
    Tile tile;
    
    while (true)
    {
      while (true)
      {
        synchronized (m_renderQueue)
        {
          tile = m_renderQueue.isEmpty() ? null : m_renderQueue.removeFirst();
        }
        
        if (tile != null)
        {
          renderTile(tile);
          repaint();
        }
        else
        {
          break;
        }
      }
      
      m_renderMonitor.waitM();
    }
  }
  
  public void transformBegin()
  {
    m_lastView = Util.viewToMap(m_content.getCoordinateReferenceSystem(), m_transform, 
      new Rectangle.Double(m_origin.x, m_origin.y, getWidth(), getHeight()));
  }
  
  public void transformEnd(AffineTransform trans)
  {
    try
    {
      trans.inverseTransform(m_origin, m_origin);
      m_transform.concatenate(trans);
    }
    catch (NoninvertibleTransformException ex)
    {
    }
  }
  
  public void zoomAll()
  {
    zoomMapArea(m_content.getMaxBounds());
  }
  
  public void zoomMapArea(ReferencedEnvelope bounds)
  {
    double  size0   = Math.max(bounds.getWidth(), bounds.getHeight());
    double  size1   = Math.min(getWidth(), getHeight());
    double  scale   = size0 / size1;
    
    if (!Double.isInfinite(scale))
    {
      transformBegin();
      m_origin.setLocation(0.0, 0.0);
      m_transform.setToTranslation(bounds.getMinX(), bounds.getMaxY());
      m_transform.scale(scale, -scale);
      highlightLastView();
      repaint();
    }
  }
}
