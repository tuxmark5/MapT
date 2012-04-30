package mapt.caster;

import java.awt.Color;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.*;
import org.opengis.filter.FilterFactory2;

public class StyleGenerator
{
  public    FilterFactory2    ff = CommonFactoryFinder.getFilterFactory2(null);
  public    StyleFactory      sf = CommonFactoryFinder.getStyleFactory(null);
  private   FeatureTypeStyle  ftStyle;
  protected Rule              rule;
  
  public enum Type
  {
    CONTOUR,
    DEFAULT,
    DISTRICT,
    FOREST,
    LAKE,
    MOUNTAIN,
    RIVER,
    SETTLEMENT,
    SURFACE,
    
    RAY,
    RAY_POINT,
    SETTLEMENT2
  }
  
  public StyleGenerator()
  {
    
  }
  
  public LineSymbolizer newLineSym(Color fillColor, float opacity)
  {
    Stroke  stroke  = sf.createStroke(ff.literal(fillColor), ff.literal(opacity));

    return sf.createLineSymbolizer(stroke, "the_geom");
  }
  
  public PolygonSymbolizer newPolySym(Color fillColor, float opacity)
  {
    Stroke  stroke  = sf.getDefaultStroke();
    Fill    fill    = sf.createFill(ff.literal(fillColor), ff.literal(opacity));

    return sf.createPolygonSymbolizer(stroke, fill, "the_geom");
  }
  
  public PointSymbolizer newPtSym(Mark mark, Color lineColor, int size)
  {
    Graphic graphic = sf.createDefaultGraphic();
    Fill    fill    = sf.createFill(ff.literal(lineColor), ff.literal(1.0f));
    
    mark.setFill(fill);
    graphic.graphicalSymbols().clear();
    graphic.graphicalSymbols().add(mark);
    graphic.setSize(ff.literal(size));
    
    return sf.createPointSymbolizer(graphic, "the_geom");
  }
  
  public Style createStyle(Type type, FeatureSource source)
  {
    Style style = sf.createStyle();
    
    ftStyle = sf.createFeatureTypeStyle();
    setupStyle(type);
    style.featureTypeStyles().add(ftStyle);
    
    return style;
  }
  
  protected Rule ruleBegin(Symbolizer symbolizer)
  {
    rule = sf.createRule();
    rule.symbolizers().add(symbolizer);
    return rule;
  }
  
  protected void ruleEnd()
  {
    ftStyle.rules().add(rule);
    rule = null;
  }
  
  public void setupStyle(Type type)
  {
    switch (type)
    {
      case CONTOUR:
        ruleBegin(newLineSym(new Color(0xD2, 0x6F, 0x30), 1.0f));
        ruleEnd();
        break;
              
      case DEFAULT:
        ruleBegin(newLineSym(Color.BLUE, 1.0f));
        ruleEnd();
        break;
        
      case DISTRICT:
        ruleBegin(newLineSym(new Color(0x91, 0x55, 0x49), 0.2f));
        ruleEnd();
        break;
        
      case FOREST:
        ruleBegin(newPolySym(new Color(0x2E, 0x6D, 0x28), 0.4f));
        ruleEnd();
        break;
        
      case LAKE:
        ruleBegin(newLineSym(new Color(0x00, 0x00, 0xFF), 1.0f));
        ruleEnd();
        ruleBegin(newPolySym(new Color(0x00, 0x00, 0xFF), 0.2f));
        ruleEnd();
        break;
        
      case MOUNTAIN:
        ruleBegin(newPtSym(sf.getTriangleMark(), Color.GREEN, 10))
          .setFilter(ff.greater(ff.property("numRays"), ff.literal(0), false));
        ruleEnd();
        ruleBegin(newPtSym(sf.getTriangleMark(), Color.RED, 10))
          .setElseFilter(true);
        ruleEnd();
        break;
        
      case RIVER:
        ruleBegin(newLineSym(Color.BLUE, 1.0f));
        ruleEnd();
        break;
        
      case SETTLEMENT:
        ruleBegin(newPolySym(new Color(0x91, 0x55, 0x49), 0.2f));
        ruleEnd();
        break;
        
      case SURFACE:
        ruleBegin(newLineSym(Color.BLUE, 1.0f));
        ruleEnd();
        break;

      case RAY: 
        ruleBegin(newLineSym(new Color(0x00, 0xFF, 0x00), 1.0f))
          .setFilter(ff.equal(ff.property("valid"), ff.literal(1), false));
        ruleEnd();
        ruleBegin(newLineSym(new Color(0xFF, 0x00, 0x00), 1.0f))
          .setElseFilter(true);
        ruleEnd();
        break;
        
      case RAY_POINT:
        ruleBegin(newPtSym(sf.getSquareMark(), Color.RED,    5))
          .setFilter(ff.equal(ff.property("valid"), ff.literal(-1), false));
        ruleEnd();
        ruleBegin(newPtSym(sf.getSquareMark(), Color.YELLOW, 5))
          .setFilter(ff.equal(ff.property("valid"), ff.literal( 0), false));
        ruleEnd();
        ruleBegin(newPtSym(sf.getSquareMark(), Color.GREEN,  5))
          .setElseFilter(true);
        ruleEnd();
        break;
        
      case SETTLEMENT2:
        ruleBegin(newPolySym(new Color(0x00, 0xFF, 0xFF), 0.2f));
        ruleEnd();
        break;
    }
  }
}
