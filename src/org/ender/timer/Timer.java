package org.ender.timer;

import haven.Coord;
import haven.Label;
import haven.UI;
import haven.Window;


public class Timer {
    public void setDuration(long duration) {
	this.duration = duration;
    }

    public interface  Callback {
	public void run(Timer timer);
    }

    private static final int SERVER_RATIO = 3;
    
    public static long server;
    public static long local;
    
    private long start;
    private long duration;
    private String name;
    transient private long remaining;
    transient public Callback updater;

    public Timer(){}

    public boolean isWorking(){
	return start != 0;
    }
    
    public void stop(){
	start = 0;
	if(updater != null){
	    updater.run(this);
	}
	TimerController.getInstance().save();
    }
    
    public void start(){
	start = server + SERVER_RATIO*(System.currentTimeMillis() - local);
	TimerController.getInstance().save();
    }
    
    public synchronized boolean update(){
	long now = System.currentTimeMillis();
	remaining = (duration - now + local - (server - start)/SERVER_RATIO);
	if(remaining <= 0){
	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str;
	    if(remaining < -1500){
		str = String.format("%s elapsed since timer named \"%s\"  finished it's work", toString(), name);
	    } else {
		str = String.format("Timer named \"%s\" just finished it's work", name);
	    }
	    new Label(Coord.z, wnd, str);
	    wnd.justclose = true;
	    wnd.pack();
	    return true;
	}
	if(updater != null){
	    updater.run(this);
	}
	return false;
    }
    
    public synchronized long getStart() {
        return start;
    }

    public synchronized void setStart(long start) {
        this.start = start;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized long getDuration()
    {
	return duration;
    }

    @Override
    public String toString() {
	long t = Math.abs(isWorking()? remaining : duration)/1000;
	int h = (int) (t/3600);
	int m = (int) ((t%3600)/60);
	int s = (int) (t%60);
	return String.format("%d:%02d:%02d", h,m,s);
    }
    
    public void destroy(){
	TimerController.getInstance().remove(this);
	updater = null;
    }
    
}