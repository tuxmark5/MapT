package mapt;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.styling.Style;
import org.geotools.swing.styling.JSimpleStyleDialog;

public class LayerTable extends JScrollPane implements ListSelectionListener
{
  private class MouseHandler implements MouseListener
  {
    @Override
    public void mouseClicked(MouseEvent e)
    {
      int   row   = m_table.rowAtPoint(e.getPoint());
      int   col   = m_table.columnAtPoint(e.getPoint());
      Layer layer = m_mapContent.layers().get(row);
      
      switch (col)
      {
        case 2: 
          m_mapContent.removeLayer(layer);
          Util.dispose(layer);
          break;
          
        case 3:
        {
          Style style = JSimpleStyleDialog.showDialog(null, (StyleLayer) layer);
          if (style != null)
            ((StyleLayer) layer).setStyle(style);
          break;
        }
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
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }
  }
  
  private MapContent                      m_mapContent;
  private ArrayList<CurrentLayerListener> m_currentLayerListeners;
  
  private JTable                          m_table;
  private LayerModel                      m_model;
  
  public LayerTable(MapContent content)
  {
    m_mapContent            = content;
    m_currentLayerListeners = new ArrayList<CurrentLayerListener>(); 
    
    m_model                 = new LayerModel(content);
    m_table                 = new JTable(m_model);
    m_table.addMouseListener(new MouseHandler());
    m_table.getSelectionModel().addListSelectionListener(this);
    
    setColumnWidths();
    setViewportView(m_table);
  }
  
  public void addCurrentLayerListener(CurrentLayerListener listener)
  {
    m_currentLayerListeners.add(listener);
  }
  
  private void setColumnWidths()
  {
    JTableHeader      header  = m_table.getTableHeader();
    TableColumnModel  model   = header.getColumnModel();
    
    for (int i = 0; i < 4; i++)
    {
      TableColumn column = model.getColumn(i);
      
      column.setMinWidth(20);
      column.setMaxWidth(20);
      column.setPreferredWidth(20);
      column.setResizable(false);
    }
  }
  
  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    List<Layer> layers  = m_mapContent.layers();
    Layer       layer   = null;
    int         index   = e.getFirstIndex();
    
    if (index >= 0 && index < layers.size())
      layer = layers.get(index);
    
    for (CurrentLayerListener l: m_currentLayerListeners)
      l.currentLayerChanged(layer);
  }
}
