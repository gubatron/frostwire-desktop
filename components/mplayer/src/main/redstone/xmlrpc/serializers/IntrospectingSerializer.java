/*
    Copyright (c) 2005 Redstone Handelsbolag

    This library is free software; you can redistribute it and/or modify it under the terms
    of the GNU Lesser General Public License as published by the Free Software Foundation;
    either version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License along with this
    library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
    Boston, MA  02111-1307  USA
*/

package redstone.xmlrpc.serializers;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcMessages;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 *  Serializes any Java object using Introspection to learn which properties that
 *  the object exposes. Note: this code is poorly tested. Use at your own risk.
 *  It does *not* support circular references; only directed graphs are supported.
 *  
 *  @author Greger Olsson
 */

public class IntrospectingSerializer implements XmlRpcCustomSerializer
{
    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#getSupportedClass()
     */
    
    public Class getSupportedClass()
    {
        return Object.class;
    }


    /*  (Documentation inherited)
     *  @see redstone.xmlrpc.XmlRpcCustomSerializer#serialize(java.lang.Object, java.io.Writer, redstone.xmlrpc.XmlRpcSerializer)
     */
    
    public void serialize(
        Object value,
        Writer writer,
        XmlRpcSerializer builtInSerializer )
        throws XmlRpcException, IOException
    {
        writer.write( "<struct>" );

        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo( value.getClass(), java.lang.Object.class );
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
    
            for ( int i = 0; i < descriptors.length; ++i )
            {
                Object propertyValue = descriptors[ i ].getReadMethod().invoke( value, ( Object[] ) null );

                if ( propertyValue != null )
                {
                    writer.write( "<member><name>" );
                    writer.write( descriptors[ i ].getDisplayName() );
                    writer.write( "</name>" );
        
                    builtInSerializer.serialize( propertyValue, writer );
                    writer.write( "</member>" );
                }
            }
        }
        catch( java.lang.Exception e )
        {
            throw new XmlRpcException( XmlRpcMessages.getString( "IntrospectingSerializer.SerializationError" ), e );
        }

        writer.write( "</struct>" );
    }
}