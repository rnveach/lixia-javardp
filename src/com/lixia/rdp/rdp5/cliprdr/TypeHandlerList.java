/* TypeHandlerList.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.1.1.1 $
 * Author: $Author: suvarov $
 * Date: $Date: 2007/03/08 00:26:41 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
 */
package com.lixia.rdp.rdp5.cliprdr;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Iterator;

import com.lixia.rdp.RdpPacket;

public class TypeHandlerList implements Iterable<TypeHandler> {
	
	ArrayList<TypeHandler> handlers = new ArrayList<TypeHandler>();
	private int count;
	
	public TypeHandlerList(){
		count = 0;
	}
	
	
	public void add(TypeHandler t){
		if(t != null){ handlers.add(t); count++; }
	}
	
	public TypeHandler getHandlerForFormat(int format){
		for(TypeHandler handler : handlers){
			if((handler != null) && handler.formatValid(format)) return handler;
		}
		return null;
	}
	
	public TypeHandlerList getHandlersForMimeType(String mimeType){
		TypeHandlerList outList = new TypeHandlerList();
		
		for(TypeHandler handler : handlers){
			if(handler.mimeTypeValid(mimeType)) outList.add(handler);
		}
		return outList;
	}
	
	public TypeHandlerList getHandlersForClipboard(DataFlavor[] dataTypes){
		TypeHandlerList outList = new TypeHandlerList();
		
		for(TypeHandler handler : handlers){
			if(handler.clipboardValid(dataTypes)) outList.add(handler);
		}
		return outList;
	}
	
	public void writeTypeDefinitions(RdpPacket data){
		for(TypeHandler handler : handlers){
			data.setLittleEndian32(handler.preferredFormat());
			data.incrementPosition(32);
		}
	}
	
	public int count(){
		return count;
	}

	public TypeHandler getFirst() {
		if(count > 0)
			return (TypeHandler) handlers.get(0);
		else return null;
	}
	
	public Iterator<TypeHandler> iterator(){
		return handlers.iterator();
	}
}
