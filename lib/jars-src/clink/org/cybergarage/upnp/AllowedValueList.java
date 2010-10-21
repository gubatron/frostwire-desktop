/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2004
*
*	File: AllowedValueList.java
*
*	Revision:
*
*	03/27/04
*		- first revision.
*	
******************************************************************/

package org.cybergarage.upnp;

import java.util.*;

public class AllowedValueList extends Vector
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////
	
	public final static String ELEM_NAME = "allowedValueList";


	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	
	public AllowedValueList() 
	{
	}
	
	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////
	
	public String getAllowedValue(int n)
	{
		return (String)get(n);
	}

}
