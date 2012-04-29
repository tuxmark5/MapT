package mapt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;

public class InteractiveMapPane extends MapPane 
  implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
  static double ZOOM_SPEED = 1.05;
  
  public enum DragMode
  {
    NONE,
    PAN,
    SELECT
  }
  
  public enum SelectMode
  {
    NONE,
    REGION,
    SELECT,
    ZOOM
  }
  
  protected Point           m_pos0;
  protected Point           m_pos1        = null;
  protected int             m_dragButton  = 0;
  protected DragMode        m_dragMode    = DragMode.NONE;
  protected SelectMode      m_selectMode  = SelectMode.NONE;
  protected Selector        m_selector;
  
  public InteractiveMapPane(MapContent content)
  {
    super(content);
    m_selector = new Selector(content);
    
    addKeyListener(this);
    addMouseListener(this);
    addMouseWheelListener(this);
    setFocusable(true);
    setFocusTraversalKeysEnabled(true);
  }
  
  public Selector getSelector()
  {
    return m_selector;
  }
  
  @Override
  public void keyTyped(KeyEvent e)
  {
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
    {
      zoomAll();
    }
  }

  @Override
  public void keyReleased(KeyEvent e)
  {
  }
    
  @Override
  public void mouseClicked(MouseEvent e)
  {
    if (e.getButton() != 1)
      return;
    
    Point       pt    = e.getPoint();
    Rectangle   rect  = new Rectangle(pt.x - 1, pt.y - 1, 3, 3);
    
    selectScreenArea(rect, e.isControlDown());
    requestFocusInWindow();
  }
  
  @Override
  public void mouseDragged(MouseEvent e)
  {
    Point pt = e.getPoint();
    
    switch (m_dragMode)
    {
      case NONE:
        break;
        
      case PAN:
        panScreen(m_pos1.x - pt.x, m_pos1.y - pt.y); 
        m_pos1 = pt;
        break;
      
      case SELECT:
        m_pos1 = pt;
        repaint();
        break;
    }
  }
  
  @Override
  public void mouseEntered(MouseEvent e)
  {
  }
  
  @Override
  public void mouseExited(MouseEvent e)
  {
  }
  
  @Override
  public void mouseMoved(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (m_dragMode != DragMode.NONE)
      return;
    
    m_pos0        = e.getPoint();
    m_pos1        = m_pos0;
    m_dragButton  = e.getButton();
    addMouseMotionListener(this);
    
    if (m_dragButton == 1)
    {
      if (m_selectMode != SelectMode.NONE)
        m_dragMode    = DragMode.SELECT;
      else if (!e.isShiftDown())
        m_dragMode    = DragMode.PAN;
      else
      {
        m_dragMode    = DragMode.SELECT;
        m_selectMode  = SelectMode.SELECT;
      }
    }
    else if (m_dragButton == 3)
    {
      m_dragMode    = DragMode.SELECT;
      m_selectMode  = SelectMode.ZOOM;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (e.getButton() != m_dragButton)
      return;

    switch (m_selectMode)
    {
      case NONE:
        break;
        
      case REGION:
        m_region = mapScreenToMap(Util.toRectangle(m_pos0, m_pos1));
        break;
        
      case SELECT:
        selectScreenArea(Util.toRectangle(m_pos0, m_pos1), e.isControlDown());
        break;
        
      case ZOOM:
        zoomScreenArea(Util.toRectangle(m_pos0, m_pos1));
        break;
    }
    
    removeMouseMotionListener(this);
    m_dragMode    = DragMode.NONE;
    m_selectMode  = SelectMode.NONE;
    m_pos1        = null;
    repaint();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    double scale = e.getWheelRotation() < 0 ? 1.0 / ZOOM_SPEED : ZOOM_SPEED;
    
    zoomScreenAt(e.getPoint(), scale);
  }
  
  public void panScreen(int dx, int dy)
  {
    m_origin.x += dx;
    m_origin.y += dy;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    
    if (m_dragMode == DragMode.SELECT && m_pos0 != null && m_pos1 != null)
    {
      int x = Math.min(m_pos0.x,  m_pos1.x);
      int y = Math.min(m_pos0.y,  m_pos1.y);
      int w = Math.abs(m_pos0.x - m_pos1.x);
      int h = Math.abs(m_pos0.y - m_pos1.y);
      
      g.setColor(new Color(0x00, 0x00, 0xFF, 0x80));
      g.fillRect(x, y, w, h);
      
      g.setColor(new Color(0x00, 0x00, 0xFF));
      g.drawRect(x, y, w, h);
    }
  }
  
  public void regionReset()
  {
    m_region = null;
    repaint();
  }
  
  public void regionSelect()
  {
    m_selectMode = SelectMode.REGION;
  }
  
  public void selectScreenArea(Rectangle rect, boolean join)
  {
    if (!join)
      m_selector.clearSelection();
    m_selector.select(mapScreenToMap(rect));
  }
  
  public void zoomIn()
  {
    zoomScreenAt(new Point(getWidth() / 2, getHeight() / 2), 1.0 / ZOOM_SPEED);
  }
  
  public void zoomOut()
  {
    zoomScreenAt(new Point(getWidth() / 2, getHeight() / 2), ZOOM_SPEED);
  }
  
  public void zoomScreenAt(Point pt, double scale)
  {
    AffineTransform trans = new AffineTransform();
    
    trans.setToScale(scale, scale);
    transformBegin();
    m_origin.x += pt.x;
    m_origin.y += pt.y;
    transformEnd(trans);
    
    highlightLastView();
    m_origin.x -= pt.x;
    m_origin.y -= pt.y;
    repaint();
  }
  
  public void zoomScreenArea(Rectangle rect)
  {
    if (rect.width < 3 || rect.height < 3)
      return;
    
    AffineTransform trans   = new AffineTransform();
    double          scaleX  = (double) rect.width  / getWidth();
    double          scaleY  = (double) rect.height / getHeight();
    double          scale   = Math.max(scaleX, scaleY);
    
    trans.scale(scale, scale);
    
    transformBegin();
    m_origin.x += rect.x;
    m_origin.y += rect.y;
    transformEnd(trans);
    
    highlightLastView();
    repaint();
  }
  
  public void zoomSelection()
  {
    ReferencedEnvelope bounds = m_selector.getSelectionBounds();
    
    bounds.expandBy(5.0);
    zoomMapArea(bounds);
  }
}
