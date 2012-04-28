package mapt.caster;

public interface Operator<A, B>
{
  public B apply(A src);
}
