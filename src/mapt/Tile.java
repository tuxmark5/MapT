package mapt;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class Tile
{
  public Image              m_buffer;
  public ReferencedEnvelope m_envelope;
  public boolean            m_valid;
  
  public Tile(int w, int h)
  {
    m_buffer    = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    m_envelope  = new ReferencedEnvelope();
    m_valid     = false;
  }
  
  public boolean equalEnvelope(ReferencedEnvelope env)
  {
    return m_envelope.equals(env);
  }
  
  public ReferencedEnvelope getEnvelope()
  {
    return m_envelope;
  }
  
  public Rectangle.Double getViewRect(int x, int y)
  {
    int w = m_buffer.getWidth(null);
    int h = m_buffer.getHeight(null);
    return new Rectangle.Double(x * w, y * h, w, h);
  }
  
  public synchronized void setEnvelope(ReferencedEnvelope env)
  {
    m_envelope  = env;
    m_valid     = false;
  }
}
