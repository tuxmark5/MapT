package mapt.caster;

import javax.swing.SwingUtilities;

public class ProgressNotifier
{
  public  ProgressListener  progressListener;
  private int               step;
  private boolean           stepRunning;
  private long              previousTime;
  
  public void stepBegin(String descr)
  {
    if (stepRunning)
      stepEnd();
    
    final int       stepId  = step;
    final String    message = "<b>" + stepId + "</b>: " + descr;
    
    this.stepRunning = true;
    
    SwingUtilities.invokeLater(new Runnable() 
    {
      @Override
      public void run()
      {
        progressListener.stepBegin(stepId, message);
      }
    });
  }
  
  public void stepEnd()
  {
    final int     s             = step++;
    final long    currentTime   = System.currentTimeMillis();
    final double  elapsed       = (double) (currentTime - previousTime) / 1000.0;
    
    this.stepRunning  = false;
    this.previousTime = currentTime;
    
    SwingUtilities.invokeLater(new Runnable() 
    {
      @Override
      public void run()
      {
        progressListener.stepEnd(s, elapsed);
      }
    });
  }
  
  public void taskBegin(final int totalSteps)
  {
    this.previousTime = System.currentTimeMillis();
    this.step         = 0;
    this.stepRunning  = false;
    
    SwingUtilities.invokeLater(new Runnable() 
    {
      @Override
      public void run()
      {
        progressListener.taskBegin(totalSteps);
      }
    });
  }
  
  public void taskEnd(final String comment)
  {
    if (stepRunning)
      stepEnd();
     
    SwingUtilities.invokeLater(new Runnable() 
    {
      @Override
      public void run()
      {
        progressListener.taskEnd(comment);
      }
    });
  }
}
