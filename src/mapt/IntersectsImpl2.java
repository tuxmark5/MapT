package mapt;

import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.filter.spatial.AbstractPreparedGeometryFilter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.BoundedSpatialOperator;

public class IntersectsImpl2 extends AbstractPreparedGeometryFilter 
  implements BinarySpatialOperator, BoundedSpatialOperator
{
  public IntersectsImpl2(FilterFactory factory, Expression e1, Expression e2)
  {
    super(factory, e1, e2);

    // backwards compat with type system
    //this.filterType = GEO
    //this.filterType = GEOMETRY_INTERSECTS;
  }

  public IntersectsImpl2(FilterFactory factory, Expression e1, Expression e2, MatchAction matchAction)
  {
    super(factory, e1, e2, matchAction);

    // backwards compat with type system
    //this.filterType = GEOMETRY_INTERSECTS;
  }
  
  @Override
  public Object accept(FilterVisitor visitor, Object extraData)
  {
    return null;//return visitor.visit(this, extraData);
  }
  
  @Override
  protected final boolean basicEvaluate(Geometry left, Geometry right)
  {
    Envelope envLeft  = left.getEnvelopeInternal();
    Envelope envRight = right.getEnvelopeInternal();
    return envRight.intersects(envLeft) && left.intersects(right);
  }

  @Override
  public boolean evaluateInternal(Geometry left, Geometry right)
  {
    switch (literals)
    {
      case BOTH:    return cacheValue;
      case RIGHT:   return rightPreppedGeom.intersects(left);
      case LEFT:    return leftPreppedGeom.intersects(right);
      default:      return basicEvaluate(left, right);
    }
  }
}
