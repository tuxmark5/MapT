package mapt.caster;

public interface Operator<A, B>
{
  public void apply(A src, B dst);
}
