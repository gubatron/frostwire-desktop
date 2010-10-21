package org.tritonus.share.sampled.mixer;

public abstract interface TControllable
{
  public abstract void setParentControl(TCompoundControl paramTCompoundControl);

  public abstract TCompoundControl getParentControl();

  public abstract void commit();
}

