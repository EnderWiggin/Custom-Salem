package org.ender.wiki;


public class Request {
    public Request(String name, Callback callback) {
	this.name = name;
	this.callback = callback;
    }

    String name;
    Callback callback = null;
    
    public static interface Callback {
	public void wiki_item_ready(Item item);
    }
}
