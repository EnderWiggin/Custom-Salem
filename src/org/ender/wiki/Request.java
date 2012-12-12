package org.ender.wiki;


public class Request {
    
    String name;
    Callback callback = null;
    Type type = Type.ITEM;
    
    public Request(String name, Callback callback, Type type) {
	this.name = name;
	this.callback = callback;
	this.type = type;
    }
    
    public static interface Callback {
	public void wiki_item_ready(Item item);
    }
    
    public static enum Type{
	ITEM, SEARCH;
    }
}
