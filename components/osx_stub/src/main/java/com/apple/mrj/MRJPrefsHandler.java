package com.apple.mrj;

public abstract interface MRJPrefsHandler
{
  public abstract void handlePrefs()
    throws IllegalStateException;
}
