package mapt;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.Layer;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

public class QueryPanel extends JPanel
{
  private MapPane             m_mapPane;
  private Selector            m_selector;
  private LayerComboBoxModel  m_layerModel;
  private Set<FeatureId>      m_features;
  
  private JPanel              m_northPanel;
  private JComboBox           m_layerCombo;
  private JTextField          m_queryField;
  private JTable              m_table;
  
  public QueryPanel(InteractiveMapPane mapPane)
  {
    m_mapPane     = mapPane;
    m_selector    = mapPane.getSelector();
    m_layerModel  = new LayerComboBoxModel(mapPane.getMapContent());
    
    m_northPanel  = new JPanel();
    m_layerCombo  = new JComboBox(m_layerModel);
    m_queryField  = new JTextField();
    m_table       = new JTable();
    initLayout();
  }
  
  /*public void currentLayerChanged(Layer layer)
  {
    m_currentLayer  = layer;
    m_features      = null;
    m_northPanel.setEnabled(m_currentLayer != null);
  }*/
  
  private void initLayout()
  {
    InvokeAction actSearch = new InvokeAction(this, "searchFeatures");
    InvokeAction actSelect = new InvokeAction(this, "selectFeatures");
    InvokeAction actShow   = new InvokeAction(this, "showSelection");
    
    JButton   btn0  = new JButton("\u23CE");
    JButton   btn1  = new JButton("\u25B2");
    JButton   btn2  = new JButton("\u25BC");
    
    m_northPanel.setLayout(new BoxLayout(m_northPanel, BoxLayout.X_AXIS));
    m_northPanel.add(m_layerCombo);
    m_northPanel.add(m_queryField);
    m_northPanel.add(btn0);
    m_northPanel.add(btn1);
    m_northPanel.add(btn2);
    
    m_queryField.addActionListener(actSearch);
    btn0.addActionListener(actSearch);
    btn1.addActionListener(actSelect);
    btn2.addActionListener(actShow);
    
    setLayout(new BorderLayout()); 
    add(m_northPanel,             BorderLayout.BEFORE_FIRST_LINE);
    add(new JScrollPane(m_table), BorderLayout.CENTER);
  }
  
  public void searchFeatures()
  {
    Layer   currentLayer  = m_layerModel.getCurrentLayer();
    Filter  filter;
    
    if (currentLayer != null)
    {
      try
      {
        filter = CQL.toFilter(m_queryField.getText());
      }
      catch (CQLException ex)
      {
        System.err.println(ex.toString());
        return;
      }
      
      if (m_mapPane.m_region != null)
      {
        FilterFactory2  ff        = m_selector.m_ff;
        String          geomName  = Selector.getGeomAttr(currentLayer);
        Filter          filter1   = new Intersects(ff, geomName, m_mapPane.m_region);
        /**/            filter    = ff.and(filter, filter1);
      }

      searchFeatures(currentLayer, filter);
    }
  }
  
  public void searchFeatures(Layer currentLayer, Filter filter)
  {
    try
    {
      FeatureSource<?, ?>         source    = currentLayer.getFeatureSource();
      FeatureCollection<?, ?>     features  = source.getFeatures(filter);
      FeatureCollectionTableModel model     = new FeatureCollectionTableModel((SimpleFeatureCollection) features);

      m_features = new HashSet<FeatureId>();
      Selector.getFeatureIds(features, m_features);

      m_table.setModel(model);
    }
    catch (IOException ex)
    {
    }
  }
  
  public void selectFeatures()
  {
    Layer currentLayer  = m_layerModel.getCurrentLayer();
    int[] selection     = m_table.getSelectedRows();
    
    m_selector.clearSelection();
    
    if (currentLayer != null)
    {
      if (selection.length == 0)
      {
        if (m_features != null)    
          m_selector.select(currentLayer, m_features);
      }
      else
      {
        FeatureCollectionTableModel model = (FeatureCollectionTableModel) m_table.getModel();

        m_features.clear();
        for (int i = 0; i < selection.length; i++)
        {
          String    name    = (String) model.getValueAt(selection[i], 0);
          FeatureId feature = new FeatureIdImpl(name);
          m_features.add(feature);
        }
      }
      
      m_selector.select(currentLayer, m_features);
    }
  }
  
  public void showSelection()
  {
    Layer           currentLayer  = m_layerModel.getCurrentLayer();
    Set<FeatureId>  selection     = m_selector.getSelection(currentLayer);

    if (selection != null)
    {
      Filter filter = m_selector.m_ff.id(selection);

      searchFeatures(currentLayer, filter); 
    }
  }
}
