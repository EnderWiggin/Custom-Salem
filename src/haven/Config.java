/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import haven.GLSettings.SettingException;
import org.ender.wiki.Wiki;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static haven.Utils.getprop;

public class Config {
    public static String authuser = getprop("haven.authuser", null);
    public static String authserv = getprop("haven.authserv", null);
    public static String defserv = getprop("haven.defserv", "127.0.0.1");
    public static URL resurl = geturl("haven.resurl", "");
    public static URL mapurl = geturl("haven.mapurl", "");
    public static URL screenurl = geturl("haven.screenurl", "http://game.salemthegame.com/mt/ss");
    public static URL manualurl = geturl("haven.manualurl", "http://www.salemthegame.com/salemj/index");
    public static URL storeurl = geturl("haven.storeurl", "http://login.salemthegame.com/portal/tostore");
    public static URL regurl = geturl("haven.regurl", "");
    public static boolean dbtext = getprop("haven.dbtext", "off").equals("on");
    public static boolean bounddb = getprop("haven.bounddb", "off").equals("on");
    public static boolean profile = getprop("haven.profile", "off").equals("on");
    public static boolean nolocalres = getprop("haven.nolocalres", "").equals("yesimsure");
    public static boolean fscache = getprop("haven.fscache", "on").equals("on");
    public static String resdir = getprop("haven.resdir", null);
    public static boolean nopreload = getprop("haven.nopreload", "no").equals("yes");
    public static String loadwaited = getprop("haven.loadwaited", null);
    public static String allused = getprop("haven.allused", null);
    public static int mainport = getint("haven.mainport", 1870);
    public static int authport = getint("haven.authport", 1871);
    public static String authmech = getprop("haven.authmech", "native");
    public static boolean softres = getprop("haven.softres", "on").equals("on");
    public static byte[] authck = null;
    public static String prefspec = "salem";
    public static final String confid = "";
    public static String userhome = System.getProperty("user.home")+"/Salem";
    public static String version;
    public static boolean show_tempers = Utils.getprefb("show_tempers", false);
    public static boolean store_map = Utils.getprefb("store_map", true);
    public static boolean radar_icons = Utils.getprefb("radar_icons", true);
    public static boolean autoopen_craftwnd = Utils.getprefb("autoopen_craftwnd", false);

    public static String currentCharName = "";
    public static Map<String, Boolean> AUTOCHOOSE = null;
    static Properties window_props;
    public static Properties options;
    private static Map<String, Object> buildinfo = new HashMap<String, Object>();

    public static boolean isUpdate;
    public static boolean isShowNames = true;
    public static boolean timestamp = true;
    public static boolean flower_study = Utils.getprefb("flower_study", false);
    public static boolean pure_mult = Utils.getprefb("pure_mult", false);
    public static boolean blink = Utils.getprefb("blink", false);
    public static GLSettings glcfg;
    public static String server;
    protected static boolean shadows = false;
    public static boolean flight = false;
    public static boolean cellshade = false;
    protected static boolean fsaa = false;
    protected static boolean water = false;
    public static boolean center = false;
    public static float brighten = Utils.getpreff("brighten", 0.0f);
    protected static boolean ss_silent = Utils.getprefb("ss_slent", false);
    protected static boolean ss_compress = Utils.getprefb("ss_compress", true);
    protected static boolean ss_ui = Utils.getprefb("ss_ui", false);
    public static boolean hptr = Utils.getprefb("hptr", false);
    public static boolean show_contents_icons = Utils.getprefb("show_contents_icons", false);
    public static Map<String, String> contents_icons;
    public static boolean menugrid_resets = Utils.getprefb("menugrid_resets", false);
    public static boolean show_radius = Utils.getprefb("show_radius", false);
    public static Map<String, ColoredRadius.Cfg> item_radius;
    public static boolean autosift = Utils.getprefb("autosift", false);
    public static boolean gobpath = Utils.getprefb("gobpath", false);
    public static boolean gobpath_color = Utils.getprefb("gobpath_color", true);
    public static Map<String, GobPath.Cfg> gobPathCfg;
    public static boolean isocam_steps = Utils.getprefb("isocam_steps", true);
    public static boolean auto_drop_bats = Utils.getprefb("auto_drop_bats", false);
    public static boolean weight_wdg = Utils.getprefb("weight_wdg", false);
    public static boolean gobble_meters = Utils.getprefb("gobble_meters", true);;

    static {
	String p;
	if((p = getprop("haven.authck", null)) != null)
	    authck = Utils.hex2byte(p);
	File f = new File(userhome);
	if(!f.exists()){
	    f.mkdirs();
	}

	loadBuildVersion();
	loadOptions();
	window_props = loadProps("windows.conf");

	loadContentsIcons();
	loadItemRadius();
	loadAutochoose();
	Wiki.init(getFile("cache"), 3);

	loadGobPathCfg();
    }

    private static void loadAutochoose() {
	String json = loadFile("autochoose.json");
	if(json != null){
	    try {
		Gson gson = (new GsonBuilder()).create();
		Type collectionType = new TypeToken<HashMap<String, Boolean>>(){}.getType();
		AUTOCHOOSE = gson.fromJson(json, collectionType);
	    }catch(Exception ignored){ }
	}
	if(AUTOCHOOSE == null){
	    AUTOCHOOSE = new HashMap<String, Boolean>();
	    AUTOCHOOSE.put("Pick", false);
	    AUTOCHOOSE.put("Open", false);
	}
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public static void  saveAutochoose() {
	synchronized (AUTOCHOOSE) {
	    Gson gson = (new GsonBuilder()).create();
	    saveFile("autochoose.json", gson.toJson(AUTOCHOOSE));
	}
    }

    private static void loadGobPathCfg() {
	String json = loadFile("gob_path.json");
	if(json != null){
	    try {
		Gson gson = GobPath.Cfg.getGson();
		Type collectionType = new TypeToken<HashMap<String, GobPath.Cfg>>(){}.getType();
		gobPathCfg = gson.fromJson(json, collectionType);
	    }catch(Exception e){
		gobPathCfg = new HashMap<String, GobPath.Cfg>();
	    }
	}
    }

    public static void saveGobPathCfg(){
	Gson gson = GobPath.Cfg.getGson();
	saveFile("gob_path.json", gson.toJson(gobPathCfg));
    }

    private static void loadBuildVersion() {
	InputStream in = Config.class.getResourceAsStream("/buildinfo");
	try {
	    try {
		if(in != null) {
		    Properties info = new Properties();
		    info.load(in);
		    for(Map.Entry<Object, Object> e : info.entrySet())
			buildinfo.put((String)e.getKey(), e.getValue());
		}
	    } finally {
		if (in != null) { in.close(); }
	    }
	} catch(IOException e) {
	    throw(new Error(e));
	}
	version = (String) buildinfo.get("git-rev");
    }

    private static void loadContentsIcons() {
	InputStream in = Config.class.getResourceAsStream("/contents_icons.json");
	try {
	    try {
		if (in != null) {
		    Gson gson = new Gson();
		    Type collectionType = new TypeToken<HashMap<String, String>>(){}.getType();
		    String json = Utils.stream2str(in);
		    contents_icons = gson.fromJson(json, collectionType);
		}
	    } catch (JsonSyntaxException ignore){
	    } finally {
		if (in != null) { in.close(); }
	    }
	} catch(IOException e) {
	    throw(new Error(e));
	}
    }

    public static void toggleRadius(){
	show_radius = !show_radius;
	Utils.setprefb("show_radius", show_radius);
    }

    private static void loadItemRadius() {
	InputStream in = Config.class.getResourceAsStream("/item_radius.json");
	try {
	    try {
		if (in != null) {
		    Gson gson = new Gson();
		    Type collectionType = new TypeToken<HashMap<String, ColoredRadius.Cfg>>(){}.getType();
		    String json = Utils.stream2str(in);
		    item_radius = gson.fromJson(json, collectionType);
		}
	    } catch (JsonSyntaxException ignore){
	    } finally {
		if (in != null) { in.close(); }
	    }
	} catch(IOException e) {
	    throw(new Error(e));
	}
	if(item_radius == null){
	    item_radius = new HashMap<String, ColoredRadius.Cfg>();
	}
    }

    public static void setCharName(String name){
	currentCharName = name;
	MainFrame.instance.setTitle(name);
    }
    
    private static void loadOptions() {
	options = loadProps("salem.cfg");
        String ver = options.getProperty("version", "");
        isUpdate = !version.equals(ver);
        shadows = options.getProperty("shadows", "false").equals("true");
        flight = options.getProperty("flight", "false").equals("true");
        cellshade = options.getProperty("cellshade", "false").equals("true");
        fsaa = options.getProperty("fsaa", "false").equals("true");
        water = options.getProperty("water", "false").equals("true");
        
        if(isUpdate){
            saveOptions();
        }
    }

    public static void saveOptions() {
	synchronized (options) {
	    //refresh from vars
	    options.setProperty("version", version);
	    options.setProperty("shadows", shadows?"true":"false");
	    options.setProperty("flight", flight?"true":"false");
	    options.setProperty("cellshade", cellshade?"true":"false");
	    options.setProperty("fsaa", fsaa?"true":"false");
	    options.setProperty("water", water?"true":"false");
	    //store it
	    saveProps(options, "salem.cfg", "Salem config file");
	}
	
    }

    public static File getFile(String name) {
	return new File(userhome, name);
    }
    
    public static File getFile() {
	return new File(userhome);
    }

    private static int getint(String name, int def) {
	String val = getprop(name, null);
	if(val == null)
	    return(def);
	return(Integer.parseInt(val));
    }

    private static URL geturl(String name, String def) {
	String val = getprop(name, def);
	if(val.equals(""))
	    return(null);
	try {
	    return(new URL(val));
	} catch(java.net.MalformedURLException e) {
	    throw(new RuntimeException(e));
	}
    }
    
    public static synchronized void setWindowOpt(String key, String value) {
	synchronized (window_props) {
	    String prev_val =window_props.getProperty(key); 
	    if((prev_val != null)&&prev_val.equals(value))
		return;
	    window_props.setProperty(key, value);
	}
	saveWindowOpt();
    }
    
    private static Properties loadProps(String name){
	File f = getFile(name);
	Properties props = new Properties();
	if (!f.exists()) {
            try {
		f.createNewFile();
	    } catch (IOException e) {
		return null;
	    }
        }
        try {
            props.load(new FileInputStream(f));
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return props;
    }
    
    private static void saveProps(Properties props, String name, String comments){
	try {
		props.store(new FileOutputStream(getFile(name)), comments);
	    } catch (IOException e) {
		System.out.println(e);
	    }
    }
    
    public static synchronized void setWindowOpt(String key, Boolean value) {
	setWindowOpt(key, value?"true":"false");
    }
    
    public static void saveWindowOpt() {
	synchronized (window_props) {
	    saveProps(window_props, "windows.conf", "Window config options");
	}
    }
    
    private static void usage(PrintStream out) {
	out.println("usage: haven.jar [OPTIONS] [SERVER[:PORT]]");
	out.println("Options include:");
	out.println("  -h                 Display this help");
	out.println("  -d                 Display debug text");
	out.println("  -P                 Enable profiling");
	out.println("  -U URL             Use specified external resource URL");
	out.println("  -r DIR             Use specified resource directory (or $SALEM_RESDIR)");
	out.println("  -A AUTHSERV[:PORT] Use specified authentication server");
	out.println("  -u USER            Authenticate as USER (together with -C)");
	out.println("  -C HEXCOOKIE       Authenticate with specified hex-encoded cookie");
	out.println("  -m AUTHMECH        Use specified authentication mechanism (`native' or `paradox')");
    }

    public static void cmdline(String[] args) {
	PosixArgs opt = PosixArgs.getopt(args, "hdPU:r:A:u:C:m:");
	if(opt == null) {
	    usage(System.err);
	    System.exit(1);
	}
	for(char c : opt.parsed()) {
	    switch(c) {
	    case 'h':
		usage(System.out);
		System.exit(0);
		break;
	    case 'd':
		dbtext = true;
		break;
	    case 'P':
		profile = true;
		break;
	    case 'r':
		resdir = opt.arg;
		break;
	    case 'A':
		int p = opt.arg.indexOf(':');
		if(p >= 0) {
		    authserv = opt.arg.substring(0, p);
		    authport = Integer.parseInt(opt.arg.substring(p + 1));
		} else {
		    authserv = opt.arg;
		}
		break;
	    case 'U':
		try {
		    resurl = new URL(opt.arg);
		} catch(java.net.MalformedURLException e) {
		    System.err.println(e);
		    System.exit(1);
		}
		break;
	    case 'u':
		authuser = opt.arg;
		break;
	    case 'C':
		authck = Utils.hex2byte(opt.arg);
		break;
	    case 'm':
		authmech = opt.arg;
	    }
	}
	if(opt.rest.length > 0) {
	    int p = opt.rest[0].indexOf(':');
	    if(p >= 0) {
		defserv = opt.rest[0].substring(0, p);
		mainport = Integer.parseInt(opt.rest[0].substring(p + 1));
	    } else {
		defserv = opt.rest[0];
	    }
	}
    }

    public static void setglpref(GLSettings pref) {
	glcfg = pref;
	try{
	    glcfg.fsaa.set(fsaa);
	    glcfg.lshadow.set(shadows);
	    glcfg.flight.set(flight);
	    glcfg.cel.set(cellshade);
	    glcfg.wsurf.set(water);
	} catch(SettingException e){}
    }

    public static void setBrighten(float val) {
	brighten = val;
	Utils.setpreff("brighten", val);
    }

    public static String loadFile(String name){
	InputStream inputStream = null;
	File file = Config.getFile(name);
	if(file.exists() && file.canRead()) {
	    try {
		inputStream = new FileInputStream(file);
	    } catch (FileNotFoundException ignored) {
	    }
	} else {
	    inputStream = Config.class.getResourceAsStream("/"+name);
	}
	if(inputStream != null) {
	    try {
	    	return Utils.stream2str(inputStream);
	    } catch (Exception ignore){
	    } finally {
		try {inputStream.close();} catch (IOException ignored) {}
	    }
	}
	return null;
    }

    public static void saveFile(String name, String data){
	File file = Config.getFile(name);
	boolean exists = file.exists();
	if(!exists){
	    try {
		//noinspection ResultOfMethodCallIgnored
		new File(file.getParent()).mkdirs();
		exists = file.createNewFile();
	    } catch (IOException ignored) {}
	}
	if(exists && file.canWrite()){
	    PrintWriter out = null;
	    try {
		out = new PrintWriter(file);
		out.print(data);
	    } catch (FileNotFoundException ignored) {
	    } finally {
		if (out != null) {
		    out.close();
		}
	    }
	}
    }

    public static GobPath.Cfg getGobPathCfg(String resname) {
	if(gobPathCfg.containsKey(resname)){
	    return gobPathCfg.get(resname);
	}
	return GobPath.Cfg.def;
    }
}
