package mapt;

import org.geotools.map.Layer;

public interface CurrentLayerListener
{
  public void currentLayerChanged(Layer layer);
}
