package mapt.caster;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import mapt.InvokeAction;
import mapt.MapFrame;
import mapt.Selector;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.filter.identity.FeatureId;

public class CartographerPanel extends JPanel implements HyperlinkListener
{
  private Cartographer  cartographer;
  private Thread        cartographerThread;
  private MapFrame      mapFrame;
  
  private JSpinner      forestHeight            = new JSpinner();
  private JSpinner      horizonRadius           = new JSpinner();
  private JSpinner      minSettlementDistance   = new JSpinner();
  private JSpinner      maxPopulationDensity    = new JSpinner();
  private JSpinner      minHeightDifference     = new JSpinner();
  private JSpinner      rayCount                = new JSpinner();
  private JSpinner      rayLength               = new JSpinner();
  private JButton       runButton               = new JButton("Run");
  private JProgressBar  progressBar             = new JProgressBar();
  private JEditorPane   stepDescription         = new JEditorPane("text/html", "<p></p>");
  private JScrollPane   stepDescriptionPane     = new JScrollPane(stepDescription);
  
  private class CartographerListner implements ProgressListener
  {
    @Override
    public void stepBegin(int step, String name)
    {
      progressBar.setValue(step);
      appendLog(name);
    }

    @Override
    public void stepEnd(int step, double elapsed)
    {
      progressBar.setValue(step + 1);
      appendLog("(Elapsed " + elapsed + " sec)<br/>");
    }

    @Override
    public void taskBegin(int totalSteps)
    {
      //stepDescription.getDocument().remove(6, WIDTH);
      progressBar.setMinimum(0);
      progressBar.setMaximum(totalSteps);
    }

    @Override
    public void taskEnd(String comment)
    {
      appendLog(comment);
      mapFrame.addLayer(StyleGenerator.Type.RAY,          cartographer.raySource);
      mapFrame.addLayer(StyleGenerator.Type.RAY_POINT,    cartographer.rayPointSource);
      mapFrame.addLayer(StyleGenerator.Type.MOUNTAIN,     cartographer.mountain2Source);
      mapFrame.addLayer(StyleGenerator.Type.SETTLEMENT2,  cartographer.settlement2Source);
      cartographer.cleanup();
      runButton.setEnabled(true);
    }
  }
  
  public CartographerPanel(MapFrame mapFrame, Cartographer cartographer)
  {
    this.cartographer       = cartographer;
    this.cartographer.progressNotifier.progressListener = new CartographerListner();
    this.mapFrame           = mapFrame;
    
    setupUI();
    runButton.addActionListener(new InvokeAction(this, "startSearch"));
    stepDescription.addHyperlinkListener(this);
  }
  
  private void addRow(int row, Component component)
  {
    GridBagConstraints c = new GridBagConstraints();
    
    c.fill        = GridBagConstraints.HORIZONTAL;
    c.gridx       = 0;
    c.gridy       = row;
    c.weightx     = 0.5;
    c.gridwidth   = 1;
    c.gridheight  = 1;
    
    if (row == 15)
    {
      c.weighty = 10.0;
      c.fill    = GridBagConstraints.BOTH;
    }

    add(component, c);
  }
  
  public void appendLog(String string) 
  {
    try
    {
      HTMLDocument  document  = (HTMLDocument) stepDescription.getDocument();
      Element       element   = document.getRootElements()[0];
      /*****/       element   = element.getElement(element.getElementCount() - 1);
      /*****/       element   = element.getElement(element.getElementCount() - 1);
      document.insertBeforeEnd(element, string);
    }
    catch (BadLocationException ex)
    {
      System.out.println(ex.toString());
    }
    catch (IOException ex)
    {
      System.out.println(ex.toString());
    }
  }
  
  @Override
  public void hyperlinkUpdate(HyperlinkEvent e)
  {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
    {
      Selector    selector  = mapFrame.getSelector();
      FeatureId   featureId = new FeatureIdImpl(e.getDescription());
      
      selector.selectFeature(cartographer.mountainSource, featureId);
    }
  }
  
  private void setupUI()
  { 
    forestHeight.setModel         (new SpinnerNumberModel(   20.0,   1.0,   100.0,   5.0));
    horizonRadius.setModel        (new SpinnerNumberModel(  500.0,   1.0, 50000.0, 100.0));
    maxPopulationDensity.setModel (new SpinnerNumberModel(  100.0,   0.0,  1000.0, 100.0));
    minHeightDifference.setModel  (new SpinnerNumberModel(   10.0,   1.0,   200.0,   5.0));
    minSettlementDistance.setModel(new SpinnerNumberModel(10000.0,   1.0, 50000.0, 100.0));
    rayCount.setModel             (new SpinnerNumberModel(      8,     3,      32,     1));
    rayLength.setModel            (new SpinnerNumberModel(  500.0,   1.0, 50000.0, 100.0));
    
    setLayout(new GridBagLayout());

    addRow(0, new JLabel("Forest height"));
    addRow(1, forestHeight);
    
    addRow(2, new JLabel("Horizon radius"));
    addRow(3, horizonRadius);
    
    addRow(4, new JLabel("Max population density"));
    addRow(5, maxPopulationDensity);
    
    addRow(6, new JLabel("Min height difference"));
    addRow(7, minHeightDifference);
    
    addRow(8, new JLabel("Min settlement distance"));
    addRow(9, minSettlementDistance);
    
    addRow(10, new JLabel("Ray count"));
    addRow(11, rayCount);
    
    addRow(12, new JLabel("Ray length"));
    addRow(13, rayLength);
    
    runButton.setMaximumSize(new Dimension(100, 25));
    addRow(14, runButton);
    
    stepDescription.setEditable(false);
    stepDescription.setEditorKit(new HTMLEditorKit());
    stepDescriptionPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    stepDescriptionPane.setPreferredSize(new Dimension(100, 350));
    addRow(15, stepDescriptionPane);
    
    progressBar.setPreferredSize(new Dimension(10, 25));
    addRow(16, progressBar);
  }
  
  public void startSearch()
  {
    cartographer.searchArea             = mapFrame.getMapPane().getSearchArea();
    cartographer.forestHeight           = (Double)  forestHeight.getValue();
    cartographer.horizonRadius          = (Double)  horizonRadius.getValue();
    cartographer.maxPopulationDensity   = (Double)  maxPopulationDensity.getValue();
    cartographer.minHeightDifference    = (Double)  minHeightDifference.getValue();
    cartographer.minSettlementDistance  = (Double)  minSettlementDistance.getValue();
    cartographer.rayCount               = (Integer) rayCount.getValue();
    cartographer.rayLength              = (Double)  rayLength.getValue();
    runButton.setEnabled(false);
    
    cartographerThread = new Thread(cartographer);
    cartographerThread.start();
  }
}
