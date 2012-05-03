package mapt.caster;

public class OpCompileRays implements Operator<Mountain, Object>
{
  public double forestHeight;
  public double minElevDiff;
  
  public OpCompileRays(double forestHeight, double minElevDiff)
  {
    this.forestHeight   = forestHeight;
    this.minElevDiff    = minElevDiff;
  }
  
  @Override
  public void apply(Mountain m, Object unused)
  {
    m.compileRays(forestHeight, minElevDiff);
  } 
}
