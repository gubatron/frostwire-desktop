package com.apple.eawt;

import java.util.EventListener;

public abstract interface ApplicationListener extends EventListener
{
  public abstract void handleAbout(ApplicationEvent paramApplicationEvent);

  public abstract void handleOpenApplication(ApplicationEvent paramApplicationEvent);

  public abstract void handleReOpenApplication(ApplicationEvent paramApplicationEvent);

  public abstract void handleOpenFile(ApplicationEvent paramApplicationEvent);

  public abstract void handlePreferences(ApplicationEvent paramApplicationEvent);

  public abstract void handlePrintFile(ApplicationEvent paramApplicationEvent);

  public abstract void handleQuit(ApplicationEvent paramApplicationEvent);
}

/* Location:           /Users/atorres/Development/workspace.frostwire/frostwire_trunk/lib/jars/stubs/AppleJavaExtensions.jar
 * Qualified Name:     com.apple.eawt.ApplicationListener
 * JD-Core Version:    0.5.4
 */