package mapt;

public class Monitor
{
  private boolean m_notify = false;
  
  public Monitor()
  {
  }
  
  public synchronized void notifyM()
  {
    m_notify = true;
    notify();
  }
  
  public synchronized void waitM()
  {
    while (!m_notify)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ex)
      {
      }
    }

    m_notify = false;
  }
}
