package mapt;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class InvokeAction implements ActionListener
{
  private Object    m_self;
  private Method    m_method;
  
  public InvokeAction(Object self, String method)
  {
    try
    {
      Class cls = self.getClass();
      
      m_self    = self;
      m_method  = cls.getMethod(method);
    }
    catch (NoSuchMethodException ex)
    {
    }
    catch (SecurityException ex)
    {
    }
  }
  
  @Override
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      m_method.invoke(m_self);
    }
    catch (IllegalAccessException ex)
    {
    }
    catch (IllegalArgumentException ex)
    {
    }
    catch (InvocationTargetException ex)
    {
    }
  }
  
  public static JButton makeButton(String text, String icon, Object self, String method)
  {
    JButton button = new JButton(text);
    
    button.addActionListener(new InvokeAction(self, method));
    button.setIcon(new ImageIcon("data/icons/" + icon));
    button.setMaximumSize(new Dimension(150, 25));
    
    return button;
  }
}
