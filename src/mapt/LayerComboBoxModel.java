package mapt;

import javax.swing.DefaultComboBoxModel;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

public class LayerComboBoxModel extends DefaultComboBoxModel 
  implements MapLayerListListener
{
  private static class Element
  {
    public Layer m_layer;
    
    public Element(Layer layer)
    {
      m_layer = layer;
    }
    
    @Override
    public String toString()
    {
      //m_layer.getFeatureSource().getDataStore().getInfo().getTitle();
      return m_layer.getFeatureSource().getName().toString();      
    }
  }
    
  private MapContent m_mapContent;

  public LayerComboBoxModel(MapContent mapContent)
  {
    m_mapContent = mapContent;
    m_mapContent.addMapLayerListListener(this);
    rebuild();
  }
  
  public Layer getCurrentLayer()
  {
    Object object = getSelectedItem();
    return object != null ? ((Element) object).m_layer : null;
  }
  
  @Override
  public void layerAdded(MapLayerListEvent mlle)
  {
    rebuild();
  }
  
  @Override
  public void layerChanged(MapLayerListEvent mlle)
  {
  }
  
  @Override
  public void layerMoved(MapLayerListEvent mlle)
  {
    rebuild();
  }
  
  @Override
  public void layerPreDispose(MapLayerListEvent mlle)
  {
  }

  @Override
  public void layerRemoved(MapLayerListEvent mlle)
  {
    rebuild();
  }
  
  private void rebuild()
  {
    removeAllElements();
    for (Layer layer: m_mapContent.layers())
      addElement(new Element(layer));
  }
}
