/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Bjorn Johannessen <johannessen.bjorn@gmail.com>
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

import haven.GLSettings.BoolSetting;

import java.util.*;
import java.awt.Color;
import java.awt.font.TextAttribute;

public class OptWnd2 extends Window {
    public static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    public static OptWnd2 instance = null;
    private Tabs body;
    private String curcam;
    private Map<String, CamInfo> caminfomap = new HashMap<String, CamInfo>();
    private Map<String, String> camname2type = new HashMap<String, String>();
    private Comparator<String> camcomp = new Comparator<String>() {
	public int compare(String a, String b) {
	    if(a.startsWith("The ")) a = a.substring(4);
	    if(b.startsWith("The ")) b = b.substring(4);
	    return(a.compareTo(b));
	}
    };
    CheckBox opt_shadow;
    CheckBox opt_aa;
    CheckBox opt_flight;
    CheckBox opt_cel;
    CheckBox opt_show_tempers;

    private static class CamInfo {
	String name, desc;
	Tabs.Tab args;
	
	public CamInfo(String name, String desc, Tabs.Tab args) {
	    this.name = name;
	    this.desc = desc;
	    this.args = args;
	}
    }

    public OptWnd2(Coord c, Widget parent) {
	super(c, new Coord(400, 340), parent, "Options");
	justclose = true;
	body = new Tabs(Coord.z, new Coord(400, 340), this) {
		public void changed(Tab from, Tab to) {
		    Utils.setpref("optwndtab", to.btn.text.text);
		    from.btn.c.y = 0;
		    to.btn.c.y = -2;
		}};
	Widget tab;

	{ /* GENERAL TAB */
	    tab = body.new Tab(new Coord(0, 0), 60, "General");
	    new Button(new Coord(0, 30), 125, tab, "Quit") {
		public void click() {
		    HackThread.tg().interrupt();
		}};
	    new Button(new Coord(135, 30), 125, tab, "Switch character") {
		public void click() {
		    ui.gui.act("lo", "cs");
		}};
	    new Button(new Coord(0, 60), 125, tab, "Log out") {
		public void click() {
		    ui.gui.act("lo");
		}};
	    /*
	    new Button(new Coord(10, 100), 125, tab, "Toggle fullscreen") {
		public void click() {
		    if(ui.fsm != null) {
			if(ui.fsm.hasfs()) ui.fsm.setwnd();
			else               ui.fsm.setfs();
		    }
		}};
	    */

	    Widget editbox = new Frame(new Coord(310, 30), new Coord(90, 100), tab);
	    new Label(new Coord(20, 10), editbox, "Edit mode:");
	    RadioGroup editmode = new RadioGroup(editbox) {
		    public void changed(int btn, String lbl) {
			Utils.setpref("editmode", lbl.toLowerCase());
		    }};
	    editmode.add("Emacs", new Coord(10, 25));
	    editmode.add("PC",    new Coord(10, 50));
	    if(Utils.getpref("editmode", "pc").equals("emacs")) editmode.check("Emacs");
	    else                                                editmode.check("PC");
	    
	    int y = 100;
	    opt_show_tempers = new CheckBox(new Coord(0, y), tab, "Always show humor numbers"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.show_tempers = val;
		    Utils.setprefb("show_tempers", val);
		}
	    };
	    opt_show_tempers.a = Config.show_tempers;
	    //opt_show_tempers.enabled = Config.plain_tempers;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Store minimap"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.store_map = val;
		    Utils.setprefb("store_map", val);
		    if(val)ui.gui.mmap.cgrid = null;
		}
	    }.a = Config.store_map;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Study protection"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.flower_study = val;
		    Utils.setprefb("flower_study", val);
		}

		{tooltip = Text.render("Leave only 'Study' option in right-click menus, if they have one.");}
		
	    }.a = Config.flower_study;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Show purity as multiplier"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.pure_mult = val;
		    Utils.setprefb("pure_mult", val);
		}

		{tooltip = Text.render("Makes purity displayed as multiplier on item icons");}
		
	    }.a = Config.pure_mult;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Radar icons"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.radar_icons = val;
		    Utils.setprefb("radar_icons", val);
		}

		{tooltip = Text.render("Objects detected by radar will be shown by icons, if available");}
		
	    }.a = Config.radar_icons;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Blink radar objects"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.blink = val;
		    Utils.setprefb("blink", val);
		}

		{tooltip = Text.render("Objects detected by radar will blink");}
		
	    }.a = Config.blink;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Take screenshots silently"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.ss_silent = val;
		    Utils.setprefb("ss_slent", val);
		}

		{tooltip = Text.render("Screenshots will be taken without showing screenshot dialog");}
		
	    }.a = Config.ss_silent;
	    
	    new CheckBox(new Coord(200, y), tab, "Compress screenshots"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.ss_compress = val;
		    Utils.setprefb("ss_compress", val);
		}

		{tooltip = Text.render("Compressed screenshots use .JPEG, non-compressed .PNG");}
		
	    }.a = Config.ss_compress;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Include UI on screenshots"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.ss_ui = val;
		    Utils.setprefb("ss_ui", val);
		}

		{tooltip = Text.render("Sets default value of include UI on screenshot dialog");}
		
	    }.a = Config.ss_ui;
	    
	    new CheckBox(new Coord(0, y += 25), tab, "Arrow home pointer"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.hptr = val;
		    Utils.setprefb("hptr", val);
		    ui.gui.mainmenu.pv = ui.gui.mainmenu.hpv && !val; 
		}

		{tooltip = Text.render("Makes home pointer display as green arrow over character head");}
	    }.a = Config.hptr;
	}

	
	{ //-* CAMERA TAB *-
	    curcam = Utils.getpref("defcam", MapView.DEFCAM);
	    tab = body.new Tab(new Coord(70, 0), 60, "Camera");

	    new Label(new Coord(10, 30), tab, "Camera type:");
	    final RichTextBox caminfo = new RichTextBox(new Coord(180, 25), new Coord(210, 180), tab, "", foundry);
	    caminfo.bg = new java.awt.Color(0, 0, 0, 64);
	    addinfo("ortho",	"Isometric Cam",	"Isometric camera centered on character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);
	    addinfo("sortho",	"Smooth Isometric Cam",	"Isometric camera centered on character with smoothed movement. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);
	    addinfo("follow",	"Follow Cam",		"The camera follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);
	    addinfo("sfollow",	"Smoot Follow Cam",	"The camera smoothly follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);
	    addinfo("free",	"Freestyle",		"You can move around freely within the larger area around character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);

	    final Tabs cambox = new Tabs(new Coord(100, 60), new Coord(300, 200), tab);
	    
	    final RadioGroup cameras = new RadioGroup(tab) {
		    public void changed(int btn, String lbl) {
			if(camname2type.containsKey(lbl))
			    lbl = camname2type.get(lbl);
			if(!lbl.equals(curcam)) {
			    setcamera(lbl);
			}
			CamInfo inf = caminfomap.get(lbl);
			if(inf == null) {
			    cambox.showtab(null);
			    caminfo.settext("");
			} else {
			    cambox.showtab(inf.args);
			    caminfo.settext(String.format("$size[12]{%s}\n\n$col[200,175,150,255]{%s}", inf.name, inf.desc));
			}
		    }};
	    List<String> clist = new ArrayList<String>();
	    for(String camtype : MapView.camtypes.keySet())
		clist.add(caminfomap.containsKey(camtype) ? caminfomap.get(camtype).name : camtype);
	    Collections.sort(clist, camcomp);
	    int y = 25;
	    for(String camname : clist)
		cameras.add(camname, new Coord(10, y += 25));
	    cameras.check(caminfomap.containsKey(curcam) ? caminfomap.get(curcam).name : curcam);
	    
	    y = 200;
	    
	    opt_aa = new CheckBox(new Coord(180, y+=25), tab, "Antialiasing"){
		@Override
		public void set(boolean val) {
		    try {
			Config.glcfg.fsaa.set(val);
		    } catch(GLSettings.SettingException e) {
			val = false;
			getparent(GameUI.class).error(e.getMessage());
			return;
		    }
		    a = val;
		    Config.fsaa = val;
		    Config.glcfg.save();
		    Config.saveOptions();
		}
	    };
	    opt_aa.a = Config.fsaa;//Config.glcfg.fsaa.val;
	    checkVideoOpt(opt_aa, Config.glcfg.fsaa);
	    
	    CheckBox chb = new CheckBox(new Coord(180, y+=25), tab, "Quality water"){
		@Override
		public void set(boolean val) {
		    try {
			Config.glcfg.wsurf.set(val);
		    } catch(GLSettings.SettingException e) {
			val = false;
			getparent(GameUI.class).error(e.getMessage());
			return;
		    }
		    a = val;
		    Config.water = val;
		    Config.glcfg.save();
		    Config.saveOptions();
		}
	    };
	    chb.a = Config.water;
	    checkVideoOpt(chb, Config.glcfg.wsurf, Text.render("If character textures glitch, try turning Per-pixel lighting on."));
	    
	    y = 200;
	    int x = 290;
	    opt_flight = new CheckBox(new Coord(x, y+=25), tab, "Per-pixel lighting"){
		@Override
		public void set(boolean val) {
		    try {
			Config.glcfg.flight.set(val);
			if(!val){
			    Config.glcfg.flight.set(false);
			    Config.glcfg.cel.set(false);
			    Config.shadows = opt_shadow.a = false;
			    Config.cellshade = opt_cel.a = false;
			}
		    } catch(GLSettings.SettingException e) {
			val = false;
			getparent(GameUI.class).error(e.getMessage());
			return;
		    }
		    a = val;
		    Config.flight = val;
		    Config.glcfg.save();
		    Config.saveOptions();
		    checkVideoOpt(opt_shadow, Config.glcfg.lshadow);
		    checkVideoOpt(opt_cel, Config.glcfg.cel);
		}
		
	    };
	    opt_flight.a = Config.flight;
	    checkVideoOpt(opt_flight, Config.glcfg.flight, Text.render("Also known as per-fragment lighting"));
	    
	    opt_shadow = new CheckBox(new Coord(x, y+=25), tab, "Shadows"){
		@Override
		public void set(boolean val) {
		    try {
			Config.glcfg.lshadow.set(val);
		    } catch(GLSettings.SettingException e) {
			val = false;
			getparent(GameUI.class).error(e.getMessage());
			return;
		    }
		    a = val;
		    Config.shadows = val;
		    Config.glcfg.save();
		    Config.saveOptions();
		}
	    };
	    opt_shadow.a = Config.shadows;
	    checkVideoOpt(opt_shadow, Config.glcfg.lshadow);
	    
	    opt_cel = new CheckBox(new Coord(x, y+=25), tab, "Cel-shading"){
		@Override
		public void set(boolean val) {
		    try {
			Config.glcfg.cel.set(val);
		    } catch(GLSettings.SettingException e) {
			val = false;
			getparent(GameUI.class).error(e.getMessage());
			return;
		    }
		    a = val;
		    Config.cellshade = val;
		    Config.glcfg.save();
		    Config.saveOptions();
		}
	    };
	    opt_cel.a = Config.cellshade;
	    checkVideoOpt(opt_cel, Config.glcfg.cel);
	    
	    y = tab.sz.y - 20;
	    new Label(new Coord(10, y), tab, "Brightness:");
	    new HSlider(new Coord(85, y+5), 200, tab, 0, 1000, (int)(Config.brighten * 1000)) {
		public void changed() {
		    Config.setBrighten(val/1000.0f);
		    ui.sess.glob.brighten();
		}
	    };
	}
	

	{ /* AUDIO TAB */
	    tab = body.new Tab(new Coord(140, 0), 60, "Audio");

	    new Label(new Coord(10, 40), tab, "Sound volume:");

	    new HSlider(new Coord(95, 45), 200, tab, 0, 1000, (int)(Audio.volume * 1000)) {
		public void changed() {
		    Audio.setvolume(val / 1000.0);
		}
	    };
	    new CheckBox(new Coord(10, 100), tab, "Music enabled") {
		public void changed(boolean val) {
		    Music.enable(val);
		}
	    };
	}

	//new Frame(new Coord(-10, 20), new Coord(420, 330), this);
	String last = Utils.getpref("optwndtab", "");
	for(Tabs.Tab t : body.tabs) {
	    if(t.btn.text.text.equals(last))
		body.showtab(t);
	}
    }
    
    private static void checkVideoOpt(CheckBox check, BoolSetting setting){
	checkVideoOpt(check, setting, null);
    }
    
    private static void checkVideoOpt(CheckBox check, BoolSetting setting, Object tooltip){
	try {
	    setting.validate(true);
	    check.enabled = true;
	    check.tooltip = tooltip;
	} catch(GLSettings.SettingException e) {
	    check.enabled = false;
	    check.tooltip = Text.render(e.getMessage());
	}
    }


    private void setcamera(String camtype) {
	curcam = camtype;
	Utils.setpref("defcam", curcam);

	MapView mv = ui.gui.map;
	if(mv != null) {
	    mv.setcam(curcam);
	}
    }

    private int getsfxvol() {
	return((int)(100 - Double.parseDouble(Utils.getpref("sfxvol", "1.0")) * 100));
    }

    private void addinfo(String camtype, String title, String text, Tabs.Tab args) {
	caminfomap.put(camtype, new CamInfo(title, text, args));
	camname2type.put(title, camtype);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn)
	    super.wdgmsg(sender, msg, args);
    }

    public static class Frame extends Widget {
	private IBox box;
	private Color bgcoplor;

	public Frame(Coord c, Coord sz, Widget parent) {
	    super(c, sz, parent);
	    box = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
	}
	
	public Frame(Coord c, Coord sz, Color bg, Widget parent) {
	   this(c, sz, parent);
	   bgcoplor = bg;
	}

	public void draw(GOut og) {
	    GOut g = og.reclip(Coord.z, sz);
	    if(bgcoplor != null){
		g.chcolor(bgcoplor);
		g.frect(box.btloff(), sz.sub(box.bisz()));
	    }
	    g.chcolor(150, 200, 125, 255);
	    box.draw(g, Coord.z, sz);
	    super.draw(og);
	}
    }

    public static void toggle() {
	UI ui = UI.instance;
	if(instance == null){
	    instance = new OptWnd2(Coord.z, ui.gui);
	} else {
	    ui.destroy(instance);
	}
    }

    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    public static void close() {
	if(instance != null){
	    UI ui = UI.instance;
	    ui.destroy(instance);
	}
    }
}