package mapt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import javax.swing.*;
import mapt.caster.Cartographer;
import mapt.caster.CartographerPanel;
import mapt.caster.StyleGenerator;
import net.infonode.docking.*;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;
import org.geotools.swing.data.JFileDataStoreChooser;

public class MapFrame extends JFrame
{
  private class CoordinateTracker implements MouseMotionListener
  {
    @Override
    public void mouseDragged(MouseEvent e)
    {
      mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
      Point.Double pt = m_mapPane.mapScreenToMap(e.getPoint());
      m_statusBar.setText(String.format("X=%7.5f Y=%7.5f", pt.x, pt.y));
    }
  }
  
  private MapContent                  m_mapContent;
  private StyleGenerator              m_styleGen;
  
  private RootWindow                  m_rootWindow;
  
  private InteractiveMapPane          m_mapPane;
  private CartographerPanel           m_cPanel;
  private LayerTable                  m_layerTable;
  private QueryPanel                  m_queryPanel;
  private JTable                      m_propertyTable;
  private JLabel                      m_statusBar;
 
  public MapFrame(Cartographer cartographer, MapContent content)
  {
    m_mapContent      = content;
    m_styleGen        = new StyleGenerator();
    
    m_mapPane         = new InteractiveMapPane(m_mapContent);
    m_mapPane.addMouseMotionListener(new CoordinateTracker());
    m_mapPane.setBackground(Color.BLACK);
    m_mapPane.grabFocus();
    
    m_cPanel          = new CartographerPanel(this, cartographer);
    m_queryPanel      = new QueryPanel(m_mapPane);
    m_layerTable      = new LayerTable(m_mapContent);
    m_propertyTable   = new JTable();
    m_propertyTable.setModel(m_mapPane.getSelector().m_propModel);
    
    setLayout(new BorderLayout());
    //createMenu();
    createToolBar();
    createDocks();
    createStatusBar();
  }

  public Layer addLayer(StyleGenerator.Type styleType, FeatureSource source) 
  {
    Style  style   = m_styleGen.createStyle(styleType, source);
    Layer  layer   = new FeatureLayer(source, style);

    layer.setSelected(false);
    m_mapContent.addLayer(layer);
    m_mapPane.zoomAll();
    return layer;
  }
  
  public void addLayer(StyleGenerator.Type styleType, URI uri) 
  {
    try
    {
      IndexedShapefileDataStore store   = new IndexedShapefileDataStore(uri.toURL(), true, true);
      SimpleFeatureSource       source  = store.getFeatureSource();
      
      addLayer(styleType, source);
      
      //SpatialIndexFeatureCollection col = new SpatialIndexFeatureCollection(source.getFeatures());
      //SpatialIndexFeatureSource     src = new SpatialIndexFeatureSource(col);
    }
    catch (MalformedURLException e)
    {
    }
    catch (IOException e)
    {
    }
  }
  
  private void createDocks()
  {
    ViewMap viewMap         = new ViewMap();
    View    mapPaneV        = new View("Map",           null, m_mapPane);
    View    mapLayerV       = new View("Layers",        null, m_layerTable);
    View    featureTableV   = new View("Features",      null, m_queryPanel);
    View    propertyTableV  = new View("Properties",    null, new JScrollPane(m_propertyTable));
    View    cPanelV         = new View("Cartographer",  null, m_cPanel);
    
    m_rootWindow = DockingUtil.createRootWindow(viewMap, true);
    createDockTheme();
    
    viewMap.addView(0, mapPaneV);
    viewMap.addView(1, mapLayerV);
    viewMap.addView(2, featureTableV);
    viewMap.addView(3, propertyTableV);
    viewMap.addView(4, cPanelV);
    
    mapPaneV.getWindowProperties().setDragEnabled(false);
    mapPaneV.getWindowProperties().setMinimizeEnabled(false);
    mapPaneV.getWindowProperties().setUndockEnabled(false);
    
    DockingWindow twE   = new TabWindow(new DockingWindow[] {mapLayerV, cPanelV});
    DockingWindow twW   = new TabWindow(propertyTableV);
    DockingWindow twS   = new TabWindow(featureTableV);
    
    //twE.setPreferredMinimizeDirection(Direction.LEFT);
    //twW.setPreferredMinimizeDirection(Direction.RIGHT);
    
    DockingWindow swN1 = new SplitWindow(true,  0.8f, mapPaneV, twW);
    DockingWindow swN0 = new SplitWindow(true,  0.2f, twE, swN1);
    DockingWindow sw   = new SplitWindow(false, 0.8f, swN0, twS);
    
    m_rootWindow.setWindow(sw);
    
    twE.minimize(Direction.LEFT);
    twW.minimize(Direction.RIGHT);
    twS.minimize(Direction.DOWN);
    
    this.add(m_rootWindow);
  }
  
  private void createDockTheme()
  {
    DockingWindowsTheme     theme = new ShapedGradientDockingTheme();
    RootWindowProperties    rp    = new RootWindowProperties();
    DockingWindowProperties dp    = rp.getDockingWindowProperties();
    
    rp.addSuperObject(theme.getRootWindowProperties());
    rp.setRecursiveTabsEnabled(false);
    
    dp.setCloseEnabled(false);
    dp.setMaximizeEnabled(true);
    dp.setMinimizeEnabled(true);
    
    m_rootWindow.getRootWindowProperties().addSuperObject(rp);
    m_rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
    m_rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
    m_rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
  }
  
  private void createMenu()
  {
    JMenuBar  menuBar   = new JMenuBar();
    
    JMenu     menuFile  = new JMenu("File");
    
    JMenuItem itemNew   = new JMenuItem("New");
    JMenuItem itemOpen  = new JMenuItem("Open");
    JMenuItem itemExit  = new JMenuItem("Exit");
    
    itemOpen.addActionListener(new InvokeAction(this, "onAddLayer"));
    
    menuFile.add(itemNew);
    menuFile.addSeparator();
    menuFile.add(itemOpen);
    menuFile.addSeparator();
    menuFile.add(itemExit);
    
    menuBar.add(menuFile);
    
    setJMenuBar(menuBar);
  }
  
  private void createStatusBar()
  {
    m_statusBar = new JLabel("");
    add(m_statusBar, BorderLayout.SOUTH);
  }
  
  private void createToolBar()
  {
    JToolBar t = new JToolBar();

    t.add(InvokeAction.makeButton("Add Layer",      "layer-add.png",     this,      "onAddLayer"));
    t.addSeparator();
    t.add(InvokeAction.makeButton("Zoom In",        "zoom-in.png",       m_mapPane, "zoomIn"));
    t.add(InvokeAction.makeButton("Zoom Out",       "zoom-out.png",      m_mapPane, "zoomOut"));
    t.add(InvokeAction.makeButton("Zoom Selection", "zoom-sel.png",      m_mapPane, "zoomSelection"));
    t.add(InvokeAction.makeButton("Zoom Extent",    "zoom-extent.png",   m_mapPane, "zoomAll"));
    t.addSeparator();
    t.add(InvokeAction.makeButton("Select Region",  "region.png",        m_mapPane, "regionSelect"));
    t.add(InvokeAction.makeButton("Reset Region",   "region-remove.png", m_mapPane, "regionReset"));
    t.addSeparator();
    t.add(InvokeAction.makeButton("GC",             "region-remove.png", this,      "runGC"));
    
    add(t, BorderLayout.PAGE_START);
  }
  
  public InteractiveMapPane getMapPane()
  {
    return m_mapPane;
  }
  
  public Selector getSelector()
  {
    return m_mapPane.getSelector();
  }
  
  public void onAddLayer()
  {
    File file = JFileDataStoreChooser.showOpenFile("shp", new File("data"), null);
    
    if (file != null)
      addLayer(StyleGenerator.Type.DEFAULT, file.toURI());
  }
  
  public void runGC()
  {
    Runtime runtime = Runtime.getRuntime();
    
    System.gc();
    System.runFinalization();
    System.out.println(runtime.freeMemory());
    System.out.println(runtime.totalMemory());
  }
}
/*
 * Ray: dir, points (x, y, h, forestmod)
 * 
 * upes miskuose & virsunes miskuose nesiskaitot
 * 
 */