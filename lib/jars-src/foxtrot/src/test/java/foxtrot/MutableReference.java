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
public class MutableReference
{
    private Object reference;

    public MutableReference(Object reference)
    {
        this.reference = reference;
    }

    public Object get()
    {
        return reference;
    }

    public void set(Object reference)
    {
        this.reference = reference;
    }

    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof MutableReference)) return false;
        final MutableReference that = (MutableReference)obj;
        return reference != null ? reference.equals(that.reference) : that.reference == null;
    }

    public int hashCode()
    {
        return reference != null ? reference.hashCode() : 0;
    }
}
