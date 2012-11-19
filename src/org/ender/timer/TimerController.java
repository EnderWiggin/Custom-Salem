package org.ender.timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TimerController extends Thread {
    private static TimerController instance;
    private static File config;
    public List<Timer> timers;
    private Properties options;
    
    public static TimerController getInstance(){
	if(instance == null){
	    instance = new TimerController();
	}
	return instance;
    }
    
    public TimerController(){
	super("Timer Thread");
	options = new Properties();
	timers = new ArrayList<Timer>();
	setDaemon(true);
	start();
    }
    
    public static void init(File folder, String server){
	config = new File(folder, String.format("timer_%s.cfg", server));
	getInstance().load();
    }
    
 // Thread main process
    @Override
    public void run() {
	while(true) {
	    synchronized (timers) {
		for(Timer timer : timers){
		    if((timer.isWorking())&&(timer.update())){
			timer.stop();
		    }
		}
	    }	    
	    try {
		sleep(1000);
	    } catch (InterruptedException e) {}
	}
    }
    
    public void add(Timer timer){
	synchronized (timers) {
	    timers.add(timer);
	}
    }
    
    public void remove(Timer timer){
	synchronized (timers) {
	    timers.remove(timer);
	}
    }

    public void load(){
	synchronized(options){
	    try {
		options.load(new FileInputStream(config));
		synchronized (timers){
		    timers.clear();
		    for(Object key : options.keySet()){
			String str = key.toString();
			if(str.indexOf("Name")>0){
			    continue;
			}
			String tmp[] = options.getProperty(str).split(",");
			try{
			    long start = Long.parseLong(tmp[0]);
			    long time = Long.parseLong(tmp[1]);
			    String name = options.getProperty(str+"Name");
			    new Timer(start, time, name);
			} catch(Exception e){e.printStackTrace();}
		    }
		}
	    } catch (FileNotFoundException e) {
	    } catch (IOException e) {
	    }
	}
    }

    public void save(){
	int i=0;
	synchronized(options){
	    options.clear();
	    synchronized (timers){
		for (Timer timer : timers){
		    options.setProperty("Timer"+i, String.format("%d,%d",timer.getStart(), timer.getTime()));
		    options.setProperty("Timer"+i+"Name", timer.getName());
		    i++;
		}
	    }
	    try {
		options.store(new FileOutputStream(config), "Timers config");
	    } catch (FileNotFoundException e) {
	    } catch (IOException e) {
	    }
	}
    }
}