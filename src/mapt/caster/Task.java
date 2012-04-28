package mapt.caster;

import java.io.IOException;

public abstract class Task
{
  public Cartographer c;
  
  public Task(Cartographer c)
  {
    this.c = c;
  }
  
  public abstract void run() throws IOException;
}
