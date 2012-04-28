package mapt.caster;

import com.vividsolutions.jts.geom.Coordinate;

public class RayPoint extends Coordinate
{
  public enum Type
  {
    START,
    END,
    FOREST_BEGIN,
    FOREST_END,
    ELEV,
    WATER
  }
  
  public Type type;
  
  public RayPoint(Type type, Coordinate coord)
  {
    super(coord);
    this.type = type;
  }  
}
