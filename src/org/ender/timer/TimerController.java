package org.ender.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.Config;
import haven.Utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class TimerController extends Thread {
    private static TimerController instance;
    private static File config;
    public List<Timer> timers;
    final public Object lock = new Object();

    public static TimerController getInstance(){
	return instance;
    }
    
    public TimerController(){
	super("Timer Thread");
	load();
	setDaemon(true);
	start();
    }
    
    public static void init(String server){
	config = Config.getFile(String.format("timer_%s.cfg", server));
	instance = new TimerController();
    }
    
 // Thread main process
    @Override
    public void run() {
	//noinspection InfiniteLoopStatement
	while(true) {
	    synchronized (lock) {
		for(Timer timer : timers){
		    if((timer.isWorking())&&(timer.update())){
			timer.stop();
		    }
		}
	    }	    
	    try {
		sleep(1000);
	    } catch (InterruptedException ignored) {}
	}
    }
    
    public void add(Timer timer){
	synchronized (lock) {
	    timers.add(timer);
	    save();
	}
    }
    
    public void remove(Timer timer){
	synchronized (lock) {
	    timers.remove(timer);
	}
    }

    private void load(){
	try {
	    Gson gson = new GsonBuilder().create();
	    InputStream is = new FileInputStream(config);
	    timers = gson.fromJson(Utils.stream2str(is), new TypeToken<List<Timer>>(){}.getType());
	} catch (Exception e) {
	    timers = new LinkedList<Timer>();
	}
    }

    public void save(){
	Gson gson = new GsonBuilder().create();
	String data = gson.toJson(timers);
	boolean exists = config.exists();
	if(!exists){
	    try {
		//noinspection ResultOfMethodCallIgnored
		new File(config.getParent()).mkdirs();
		exists = config.createNewFile();
	    } catch (IOException ignored) {}
	}
	if(exists && config.canWrite()){
	    PrintWriter out = null;
	    try {
		out = new PrintWriter(config);
		out.print(data);
	    } catch (FileNotFoundException ignored) {
	    } finally {
		if (out != null) {
		    out.close();
		}
	    }
	}
    }
}