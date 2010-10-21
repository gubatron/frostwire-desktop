package com.apple.mrj;

public class MRJOSType
{
  final int itsValue;
  public static final MRJOSType kTypeTEXT = new MRJOSType(1413830740);
  public static final MRJOSType kTypeUtxt = new MRJOSType(1970567284);
  public static final MRJOSType kTypeStyl = new MRJOSType(1937013100);
  public static final MRJOSType kTypeUstl = new MRJOSType(1970500716);
  public static final MRJOSType kTypePICT = new MRJOSType(1346978644);
  public static final MRJOSType kTypeHFS = new MRJOSType(1751544608);
  public static final MRJOSType kTypeURL = new MRJOSType(1970433056);
  public static final MRJOSType kTypeGIFF = new MRJOSType(1195984454);
  public static final MRJOSType kTypeJPEG = new MRJOSType(1246774599);
  public static final MRJOSType kTypeMoov = new MRJOSType(1836019574);
  public static final MRJOSType kTypeJser = new MRJOSType(1785947506);

  public MRJOSType(String paramString)
  {
    byte[] arrayOfByte = new byte[4];
    int i = paramString.length();
    if (i > 0)
    {
      if (i > 4)
        i = 4;
      paramString.getBytes(0, i, arrayOfByte, 4 - i);
    }
    this.itsValue = bytesToInt(arrayOfByte);
  }

  public MRJOSType(int paramInt)
  {
    this.itsValue = paramInt;
  }

  public MRJOSType(byte[] paramArrayOfByte)
  {
    this.itsValue = bytesToInt(paramArrayOfByte);
  }

  public final boolean equals(MRJOSType paramMRJOSType)
  {
    return this.itsValue == paramMRJOSType.itsValue;
  }

  public final boolean equals(int paramInt)
  {
    return this.itsValue == paramInt;
  }

  public final boolean equals(Object paramObject)
  {
    return (paramObject == this) || ((paramObject instanceof MRJOSType) && (equals((MRJOSType)paramObject)));
  }

  public int hashCode()
  {
    return this.itsValue;
  }

  public int toInt()
  {
    return this.itsValue;
  }

  public String toString()
  {
    byte[] arrayOfByte = { (byte)(this.itsValue >> 24), (byte)(this.itsValue >> 16), (byte)(this.itsValue >> 8), (byte)this.itsValue };
    return new String(arrayOfByte, 0);
  }

  private static int bytesToInt(byte[] paramArrayOfByte)
  {
    int i = 0;
    int j = paramArrayOfByte.length;
    if (j > 4)
      j = 4;
    for (int k = 0; k < j; ++k)
    {
      if (k > 0)
        i <<= 8;
      i |= paramArrayOfByte[k] & 0xFF;
    }
    return i;
  }
}
