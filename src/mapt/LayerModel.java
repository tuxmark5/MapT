package mapt;

import javax.swing.table.AbstractTableModel;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

public class LayerModel extends AbstractTableModel implements MapLayerListListener
{
  private MapContent m_mapContent;
  
  public LayerModel(MapContent content)
  {
    m_mapContent = content;
    m_mapContent.addMapLayerListListener(this);
  }
  
  @Override
  public int getColumnCount()
  {
    return 5;
  }
  
  @Override
  public Class getColumnClass(int column) 
  {
    switch (column)
    {
      case 0: return Boolean.class;
      case 1: return Boolean.class;
      case 2: return String.class;
      case 3: return String.class;
      case 4: return String.class;
    }
    
    return Object.class;
  }
  
  @Override
  public String getColumnName(int column)
  {
    switch (column)
    {
      case 0: return "V";
      case 1: return "S";
      case 2: return "R";
      case 3: return "P";
      case 4: return "Name";
    }
    
    return "?";
  }
  
  @Override
  public int getRowCount()
  {
    return m_mapContent.layers().size();
  }
  
  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    Layer layer = m_mapContent.layers().get(rowIndex);
    
    switch (columnIndex)
    {
      case 0: return (Boolean) layer.isVisible();
      case 1: return (Boolean) layer.isSelected();
      case 2: return "X";
      case 3: return "P";
      case 4: return layer.getFeatureSource().getName().toString();
    }
    
    return null;
  }
  
  @Override
  public boolean isCellEditable(int row, int column)
  {
    switch (column)
    {
      case 0: return true;
      case 1: return true;
    }
    
    return false;
  }
  
  @Override
  public void layerAdded(MapLayerListEvent mlle)
  {
    fireTableRowsInserted(mlle.getToIndex(), mlle.getToIndex());
  }
  
  @Override
  public void layerChanged(MapLayerListEvent mlle)
  {
    fireTableRowsUpdated(mlle.getToIndex(), mlle.getToIndex());
  }
  
  @Override
  public void layerMoved(MapLayerListEvent mlle)
  {
    fireTableDataChanged();
  }
  
  @Override
  public void layerPreDispose(MapLayerListEvent mlle)
  {
  }

  @Override
  public void layerRemoved(MapLayerListEvent mlle)
  {
    fireTableDataChanged();
  }
  
  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex)
  {
    Layer layer = m_mapContent.layers().get(rowIndex);
    
    switch (columnIndex)
    {
      case 0: layer.setVisible((Boolean) value); break;
      case 1: layer.setSelected((Boolean) value); break;
    }
  }
}
