package mapt.caster;

public class OpCompileRays implements Operator<Mountain, Object>
{
  public double forestHeight;
  
  public OpCompileRays(double forestHeight)
  {
    this.forestHeight = forestHeight;
  }
  
  @Override
  public void apply(Mountain m, Object unused)
  {
    m.compileRays(forestHeight);
  } 
}
