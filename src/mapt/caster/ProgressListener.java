package mapt.caster;

public interface ProgressListener
{
  public void stepBegin(int step, String name);
  public void stepEnd(int step, double elapsed);
  public void taskBegin(int totalSteps);
  public void taskEnd(String comment);
}
