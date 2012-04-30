package mapt;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.awt.Color;
import java.io.IOException;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.Polygon;

public class Selector
{
  public enum GeomType
  {
    POINT,
    LINE,
    POLYGON
  }
  
  private static final Color SELECTED_COLOUR  = Color.YELLOW;
  private static final float OPACITY          = 1.0f;
  private static final float LINE_WIDTH       = 1.0f;
  private static final float POINT_SIZE       = 10.0f;
  
  protected MapContent                  m_mapContent;
  public    FilterFactory2              m_ff            = CommonFactoryFinder.getFilterFactory2(null);
  protected StyleFactory                m_sf            = CommonFactoryFinder.getStyleFactory(null);
  protected SimpleFeatureSource         m_featureSource = null;
  protected Map<Layer, Set<FeatureId>>  m_selection     = new HashMap<Layer, Set<FeatureId>>();
  public    DefaultTableModel           m_propModel     = new DefaultTableModel();
    
  public Selector(MapContent mapContent)
  {
    m_mapContent = mapContent;
    m_propModel.addColumn("Property");
    m_propModel.addColumn("Value");
  }
  
  public void clearSelection()
  {
    m_selection.clear();
  }
  
  private Rule createSelectionRule(Layer layer, Set<FeatureId> ids) 
  {
    FeatureSource<?, ?> source        = layer.getFeatureSource();
    String              geomAttr      = source.getSchema().getGeometryDescriptor().getLocalName();
    GeomType            geomType      = getFeatureSourceGeomType(source);
    Rule                rule          = createRule(geomAttr, geomType, SELECTED_COLOUR, SELECTED_COLOUR);
    
    rule.setName("selection");
    rule.setFilter(m_ff.id(ids));
    return rule;
  }
 
  private Rule createRule(String geomAttr, GeomType geomType, Color outlineColor, Color fillColor)
  {
    Fill        fill;
    Symbolizer  symbolizer  = null;
    Stroke      stroke      = m_sf.createStroke(m_ff.literal(outlineColor), m_ff.literal(LINE_WIDTH));
    Rule        rule        = m_sf.createRule();
    
    switch (geomType)
    {
      case POLYGON:
        fill        = m_sf.createFill(m_ff.literal(fillColor), m_ff.literal(OPACITY));
        symbolizer  = m_sf.createPolygonSymbolizer(stroke, fill, geomAttr);
        break;

      case LINE:
        symbolizer  = m_sf.createLineSymbolizer(stroke, geomAttr);
        break;

      case POINT:
        Mark    mark    = m_sf.getCircleMark();
        Graphic graphic = m_sf.createDefaultGraphic();
        /**/    fill    = m_sf.createFill(m_ff.literal(fillColor), m_ff.literal(OPACITY));
        
        mark.setFill(fill);
        mark.setStroke(stroke);
        
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);
        graphic.setSize(m_ff.literal(POINT_SIZE));

        symbolizer = m_sf.createPointSymbolizer(graphic, geomAttr);
        break;
    }

    rule.symbolizers().add(symbolizer);
    return rule;
  }

  public void filterFeatures(Layer layer, Filter filter, Set<FeatureId> ids)
  {   
    try
    {
      FeatureCollection<?, ?> selection = layer.getFeatureSource().getFeatures(filter);
      Feature                 feature0  = getFeatureIds(selection, ids);
      
      populateProperties(ids.size() == 1 ? feature0 : null);
    }
    catch (IOException ex)
    {
    }
  }
  
  public static Feature getFeatureIds(FeatureCollection<?, ?> collection, Set<FeatureId> ids)
  {
    Feature             feature0  = null;
    FeatureIterator<?>  iter      = collection.features();

    try 
    {
      if (iter.hasNext())
      {
        feature0 = iter.next();
        ids.add(feature0.getIdentifier());
      }
      
      while (iter.hasNext()) 
      {
        ids.add(iter.next().getIdentifier());
      }
    } 
    finally 
    {
      iter.close();
    }
    
    return feature0;
  }
  
  public static String getGeomAttr(Layer layer)
  {
    FeatureType schema = layer.getFeatureSource().getSchema();
    
    return schema.getGeometryDescriptor().getLocalName();
  }
  
  public GeomType getFeatureSourceGeomType(FeatureSource<?, ?> source)
  {
    GeometryDescriptor  geomDesc = source.getSchema().getGeometryDescriptor();
    Class<?>            instance = geomDesc.getType().getBinding();

    if (Polygon.class.isAssignableFrom(instance))
      return GeomType.POLYGON;
    else if(MultiPolygon.class.isAssignableFrom(instance)) 
      return GeomType.POLYGON;
    else if (LineString.class.isAssignableFrom(instance))
      return GeomType.LINE;
    else if (MultiLineString.class.isAssignableFrom(instance))
      return GeomType.LINE;
    return GeomType.POINT;
  }
  
  public MapContent getMapContent()
  {
    return m_mapContent;
  }
  
  public Set<FeatureId> getSelection(Layer layer)
  {
    return m_selection.get(layer);
  }
  
  public ReferencedEnvelope getSelectionBounds()
  {
    ReferencedEnvelope box0 = new ReferencedEnvelope();
    
    for (Map.Entry<Layer, Set<FeatureId>> entry: m_selection.entrySet()) 
    {
      Layer   layer   = entry.getKey();
      Filter  filter  = m_ff.id(entry.getValue());
      
      try
      {
        FeatureCollection<?, ?> features  = layer.getFeatureSource().getFeatures(filter);
        FeatureIterator<?>      iter      = features.features();
        
        try 
        {
          while (iter.hasNext()) 
          {
            BoundingBox box = iter.next().getBounds();
            box0.expandToInclude(box.getMinX(), box.getMinY());
            box0.expandToInclude(box.getMaxX(), box.getMaxY());
          }
        } 
        finally 
        {
          iter.close();
        }
      }
      catch (IOException ex)
      {
        
      }
    }
    
    return box0;
  }
  
  public void populateProperties(Feature feature)
  {
    while (m_propModel.getRowCount() > 0)
    {
      m_propModel.removeRow(0);
    }
    
    if (feature != null)
    {
      for (Property p: feature.getProperties())
      {
        Object[] row = new Object[2];
        row[0] = p.getName().toString();
        row[1] = p.getValue().toString();
        m_propModel.addRow(row);
      }
    }
  }
  
  public void removeSelectionRule(FeatureTypeStyle ftStyle)
  {
    List<Rule> rules = ftStyle.rules();
    
    for (int i = 0; i < rules.size(); )
    {
      Rule    rule = rules.get(i);
      String  name = rule.getName();
      
      if (name != null && name.equals("selection"))
        rules.remove(i);
      else
        i++;
    }
  }

  public void select(ReferencedEnvelope env)
  {
    for (Layer layer: m_mapContent.layers())
    {
      if (layer.isSelected())
      {
        select(layer, env);
      }
    }
  }
  
  public void select(Layer layer, Filter filter)
  {
    Set<FeatureId> selection = m_selection.get(layer);
    
    if (selection == null)
      selection = new HashSet<FeatureId>();
    
    filterFeatures(layer, filter, selection);

    select(layer, selection);
  }
  
  public void select(Layer layer, ReferencedEnvelope env)
  {
    String geomName  = getGeomAttr(layer);
    Filter filter    = m_ff.intersects(m_ff.property(geomName), m_ff.literal(env));
    
    select(layer, filter);
  }
  
  public void select(Layer layer, Set<FeatureId> selection)
  {
    Style             style     = layer.getStyle();
    FeatureTypeStyle  ftStyle   = style.featureTypeStyles().get(0);
    
    m_selection.put(layer, selection);
    removeSelectionRule(ftStyle);
    
    if (!selection.isEmpty()) 
      ftStyle.rules().add(0, createSelectionRule(layer, selection));
    
    // TODO: find a way to update view
    layer.setVisible(!layer.isVisible());
    layer.setVisible(!layer.isVisible());
  }
  
  public void selectFeature(FeatureSource source, FeatureId featureId)
  {
    for (Layer layer: m_mapContent.layers())
    {
      if (layer.getFeatureSource() == source)
      {
        Set<FeatureId> selection = new HashSet<FeatureId>();
        selection.add(featureId);
        select(layer, selection);
      }
    }
  }
  
  public void setFeatureSource(SimpleFeatureSource sf)
  {
    m_featureSource = sf;
  }
}
