/**
 * Copyright (c) 2002-2008, Simone Bordet
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package foxtrot;

/**
 * @version $Revision: 263 $
 */
public class MutableInteger implements Comparable
{
    private int value;

    public MutableInteger(int value)
    {
        this.value = value;
    }

    public int get()
    {
        return value;
    }

    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof MutableInteger)) return false;
        MutableInteger that = (MutableInteger)obj;
        return value == that.value;
    }

    public int hashCode()
    {
        return value;
    }

    public int compareTo(Object obj)
    {
        if (obj == null) return 1;
        if (obj == this) return 0;
        int thisValue = get();
        int otherValue = ((MutableInteger)obj).get();
        if (thisValue > otherValue) return 1;
        if (thisValue < otherValue) return -1;
        return 0;
    }

    public void set(int value)
    {
        this.value = value;
    }

    public String toString()
    {
        return String.valueOf(get());
    }
}
