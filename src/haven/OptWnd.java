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

import java.util.*;
import java.awt.Color;
import java.awt.font.TextAttribute;

public class OptWnd extends Window {
    public static final RichText.Foundry foundry = new RichText.Foundry(TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, 10);
    public static OptWnd instance = null;
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

    public OptWnd(Coord c, Widget parent) {
	super(c, new Coord(400, 340), parent, "Options");
	justclose = true;
	body = new Tabs(Coord.z, new Coord(400, 300), this) {
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
	    new Button(new Coord(0, 60), 125, tab, "Log out") {
		public void click() {
		    ui.sess.close();
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
	    new CheckBox(new Coord(0, y), tab, "Show humors as bars"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.plain_tempers = val;
		    opt_show_tempers.enabled = val;
		    Utils.setprefb("plain_tempers", val);
		}
	    }.a = Config.plain_tempers;
	    
	    opt_show_tempers = new CheckBox(new Coord(0, y += 30), tab, "Always show humor numbers"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.show_tempers = val;
		    Utils.setprefb("show_tempers", val);
		}
	    };
	    opt_show_tempers.a = Config.show_tempers;
	    opt_show_tempers.enabled = Config.plain_tempers;
	    
	    new CheckBox(new Coord(0, y += 30), tab, "Store minimap"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    Config.store_map = val;
		    Utils.setprefb("store_map", val);
		    if(val)ui.gui.mmap.cgrid = null;
		}
	    }.a = Config.store_map;
	}

	
	{ //-* CAMERA TAB *-
	    curcam = Utils.getpref("defcam", MapView.DEFCAM);
	    tab = body.new Tab(new Coord(70, 0), 60, "Camera");

	    new Label(new Coord(10, 40), tab, "Camera type:");
	    final RichTextBox caminfo = new RichTextBox(new Coord(180, 25), new Coord(210, 180), tab, "", foundry);
	    caminfo.bg = new java.awt.Color(0, 0, 0, 64);
	    addinfo("follow",       "Follow Cam",  "The camera follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);
	    addinfo("sfollow",    "Follow Cam Smoothed", "The camera smoothly follows the character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);
	    addinfo("free",     "Freestyle",     "You can move around freely within the larger area around character. Use mousewheel scrolling to zoom in and out. Drag with middle mouse button to rotate camera.", null);

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
	    
	    opt_shadow = new CheckBox(new Coord(180, 225), tab, "Shadows"){
		@Override
		public void changed(boolean val) {
		    super.changed(val);
		    ui.gui.shadows = val; 
		    ui.gui.togglesdw = true;
		}
		
	    };
	    opt_shadow.a = ui.gui.shadows;
	}
	

	{ /* AUDIO TAB */
	    tab = body.new Tab(new Coord(140, 0), 60, "Audio");

	    new Label(new Coord(10, 40), tab, "Sound volume:");
	    new Frame(new Coord(10, 65), new Coord(20, 206), tab);
	    final Label sfxvol = new Label(new Coord(35, 69 + (int)(getsfxvol() * 1.86)),  tab, String.valueOf(100 - getsfxvol()) + " %");
	    new Scrollbar(new Coord(25, 70), 196, tab, 0, 100) {{ val = getsfxvol(); }
		public void changed() {
		    Audio.setvolume((100 - val) / 100.0);
		    sfxvol.c.y = 69 + (int)(val * 1.86);
		    sfxvol.settext(String.valueOf(100 - val) + " %");
		}
		public boolean mousewheel(Coord c, int amount) {
		    val = Utils.clip(val + amount, min, max);
		    changed();
		    return(true);
		}};
	    new CheckBox(new Coord(10, 280), tab, "Music enabled") {
		public void changed(boolean val) {
		    Music.enable(val);
		}};
	}

	new Frame(new Coord(-10, 20), new Coord(420, 330), this);
	String last = Utils.getpref("optwndtab", "");
	for(Tabs.Tab t : body.tabs) {
	    if(t.btn.text.text.equals(last))
		body.showtab(t);
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
		g.frect(box.tloff(), sz.sub(box.bisz()));
	    }
	    g.chcolor(150, 200, 125, 255);
	    box.draw(g, Coord.z, sz);
	    super.draw(og);
	}
    }

    public static void toggle() {
	UI ui = UI.instance;
	if(instance == null){
	    instance = new OptWnd(Coord.z, ui.gui);
	} else {
	    ui.destroy(instance);
	}
    }

    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }
}
