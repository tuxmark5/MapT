package mapt.caster;

public class OpCompileRays implements Operator<Mountain, Object>
{
  public double forestHeight;
  
  public OpCompileRays(double forestHeight)
  {
    this.forestHeight = forestHeight;
  }
  
  @Override
  public Object apply(Mountain m)
  {
    m.compileRays(forestHeight);
    return null;
  } 
}
