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

import java.awt.Color;
import java.awt.Font;
import java.awt.image.*;
import java.util.*;
import static haven.PUtils.*;

public class Window extends Widget implements DTarget {
    private static final Tex tleft = Resource.loadtex("gfx/hud/wnd/tleft");
    private static final Tex tmain = Resource.loadtex("gfx/hud/wnd/tmain");
    private static final Tex tright = Resource.loadtex("gfx/hud/wnd/tright");
    public static final BufferedImage[] cbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/wnd/cbtn"),
	Resource.loadimg("gfx/hud/wnd/cbtnd"),
	Resource.loadimg("gfx/hud/wnd/cbtnh")};
    public static final BufferedImage[] lbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/wnd/lbtn"),
	Resource.loadimg("gfx/hud/wnd/lbtnd"),
	Resource.loadimg("gfx/hud/wnd/lbtnh")};
    public static final BufferedImage[] rbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/wnd/rbtn"),
	Resource.loadimg("gfx/hud/wnd/rbtnd"),
	Resource.loadimg("gfx/hud/wnd/rbtnh")};
    public static final Color cc = new Color(248, 230, 190);
    public static final Text.Furnace cf = new Text.Imager(new Text.Foundry(new Font("Serif", Font.BOLD, 15), cc).aa(true)) {
	    protected BufferedImage proc(Text text) {
		return(rasterimg(blurmask2(text.img.getRaster(), 1, 1, Color.BLACK)));
	    }
	};
    public static final IBox fbox = new IBox("gfx/hud", "ftl", "ftr", "fbl", "fbr", "fl", "fr", "ft", "fb");
    public static final IBox swbox = new IBox("gfx/hud", "stl", "str", "sbl", "sbr", "sl", "sr", "st", "sb");
    public static final IBox wbox = new IBox("gfx/hud/wnd", "tl", "tr", "bl", "br", "vl", "vr", "ht", "hb");
    private static final IBox topless = new IBox(Tex.empty, Tex.empty, wbox.cbl, wbox.cbr, wbox.bl, wbox.br, Tex.empty, wbox.bb);
    private static final int th = tleft.sz().y, tdh = th - tmain.sz().y, tc = tdh + 18;
    private static final Coord capc = new Coord(20, th - 3);
    public Coord mrgn = new Coord(5, 5);
    protected Text cap;
    private boolean dt = false;
    protected boolean dm = false;
    public Coord ctl, csz, atl, asz, ac;
    protected Coord doff;
    protected final IButton cbtn;
    private final Collection<Widget> twdgs = new LinkedList<Widget>();

// ******************************
    private static final String OPT_POS = "_pos";
//    static Tex bg = Resource.loadtex("gfx/hud/bgtex");
//    static Tex cl = Resource.loadtex("gfx/hud/cleft");
//    static Tex cm = Resource.loadtex("gfx/hud/cmain");
//    static Tex cr = Resource.loadtex("gfx/hud/cright");
    public Coord tlo, rbo;
    public boolean justclose = false;
    protected final String name;
    @RName("wnd")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    if(args.length < 2)
		return(new Window(c, (Coord)args[0], parent, null));
	    else
		return(new Window(c, (Coord)args[0], parent, (String)args[1]));
	}
    }

    public Window(Coord c, Coord sz, Widget parent, String cap) {
	super(c, new Coord(0, 0), parent);
	if(cap != null){
	    this.cap = cf.render(cap);
	    name = cap;
	} else {
	    this.cap = null;
	    name = null;
	}
	resize(sz);
	setfocustab(true);
	parent.setfocus(this);
	cbtn = new IButton(Coord.z, this, cbtni[0], cbtni[1], cbtni[2]);
	cbtn.recthit = true;
	addtwdg(cbtn);
	loadOpts();
    }

    public Coord contentsz() {
	Coord max = new Coord(0, 0);
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if(twdgs.contains(wdg))
		continue;
	    if(!wdg.visible)
		continue;
	    Coord br = wdg.c.add(wdg.sz);
	    if(br.x > max.x)
		max.x = br.x;
	    if(br.y > max.y)
		max.y = br.y;
	}
	return(max.sub(1, 1));
    }

    protected void placetwdgs() {
	int x = sz.x - 5;
	for(Widget ch : twdgs) {
	    if(ch.visible){
		ch.c = xlate(new Coord(x -= ch.sz.x + 5, tc - (ch.sz.y / 2)), false);
	    }
	}
    }

    public void addtwdg(Widget wdg) {
	twdgs.add(wdg);
	placetwdgs();
    }

    public void resize(Coord sz) {
	IBox box;
	int th;
	if(cap == null){
	    box = wbox;
	    th = 0;
	} else {
	    box = topless;
	    th = Window.th;
	}
	sz = sz.add(box.bisz()).add(0, th).add(mrgn.mul(2));
	this.sz = sz;
	ctl = box.btloff().add(0, th);
	csz = sz.sub(box.bisz()).sub(0, th);
	atl = ctl.add(mrgn);
	asz = csz.sub(mrgn.mul(2));
	ac = new Coord();
	//ac = tlo.add(wbox.btloff()).add(mrgn);
	placetwdgs();
	for(Widget ch = child; ch != null; ch = ch.next)
	    ch.presize();
    }

    public Coord xlate(Coord c, boolean in) {
	if(in)
	    return(c.add(atl));
	else
	    return(c.sub(atl));
    }

    public void cdraw(GOut g) {
    }

    public void draw(GOut g) {
	g.chcolor(0, 0, 0, 160);
	if(ctl == null || csz == null){return;}
	g.frect(ctl, csz);
	g.chcolor();
	cdraw(g.reclip(xlate(Coord.z, true), asz));
	if(cap != null){
	    topless.draw(g, new Coord(0, th), sz.sub(0, th));
	    g.image(tleft, Coord.z);
	    Coord tmul = new Coord(tleft.sz().x, tdh);
	    Coord tmbr = new Coord(sz.x - tright.sz().x, th);
	    for(int x = tmul.x; x < tmbr.x; x += tmain.sz().x) {
		g.image(tmain, new Coord(x, tdh), tmul, tmbr);
	    }
	    g.image(tright, new Coord(sz.x - tright.sz().x, tdh));
	    g.image(cap.tex(), capc.sub(0, cap.sz().y));
	} else {
	    wbox.draw(g, Coord.z, sz);
	}
	/*
	if(cap != null) {
	    GOut cg = og.reclip(new Coord(0, -7), sz.add(0, 7));
	    int w = cap.tex().sz().x;
	    cg.image(cl, new Coord((sz.x / 2) - (w / 2) - cl.sz().x, 0));
	    cg.image(cm, new Coord((sz.x / 2) - (w / 2), 0), new Coord(w, cm.sz().y));
	    cg.image(cr, new Coord((sz.x / 2) + (w / 2), 0));
	    cg.image(cap.tex(), new Coord((sz.x / 2) - (w / 2), 0));
	}
	*/
	super.draw(g);
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "pack") {
	    pack();
	} else if(msg == "dt") {
	    dt = (Integer)args[0] != 0;
	} else {
	    super.uimsg(msg, args);
	}
    }

    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	raise();
	if(super.mousedown(c, button))
	    return(true);
	if(c.y < tdh && cap != null)
	    return(false);
	if(button == 1) {
	    ui.grabmouse(this);
	    dm = true;
	    doff = c;
	}
	return(true);
    }

    public boolean mouseup(Coord c, int button) {
	if(dm) {
	    canceldm();
	    storeOpt(OPT_POS, this.c);
	} else {
	    super.mouseup(c, button);
	}
	return(true);
    }

    public void canceldm() {
	if(dm)
	    ui.grabmouse(null);
	dm = false;
    }
	
    public void mousemove(Coord c) {
	if(dm) {
	    this.c = this.c.add(c.add(doff.inv()));
	} else {
	    super.mousemove(c);
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == cbtn) {
	    if(justclose)
		ui.destroy(this);
	    else
		wdgmsg("close");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(super.type(key, ev))
	    return(true);
	if(key == 27) {
	    if(justclose)
		ui.destroy(this);
	    else
		wdgmsg("close");
	    return(true);
	}
	return(false);
    }

    public boolean drop(Coord cc, Coord ul) {
	if(dt) {
	    wdgmsg("drop", cc);
	    return(true);
	}
	return(false);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public Object tooltip(Coord c, Widget prev) {
	Object ret = super.tooltip(c, prev);
	if(ret != null)
	    return(ret);
	else
	    return("");
    }
    
    protected void storeOpt(String opt, String value){
	if(name == null){return;}
	Config.setWindowOpt(name+opt, value);
    }
    
    protected void storeOpt(String opt, Coord value){
	storeOpt(opt, value.toString());
    }
    
    protected void storeOpt(String opt, boolean value){
	if(name == null){return;}
	Config.setWindowOpt(name+opt, value);
    }
    
    protected Coord getOptCoord(String opt, Coord def){
	synchronized (Config.window_props) {
	    try {
		return new Coord(Config.window_props.getProperty(name+opt, def.toString()));
	    } catch (Exception e){
		return def;
	    }
	}
    }
    
    protected boolean getOptBool(String opt, boolean def){
	synchronized (Config.window_props) {
	    try {
		return Config.window_props.getProperty(name+opt, null).equals("true");
	    } catch (Exception e){
		return def;
	    }
	}
    }
    
    protected void loadOpts(){
	if(name == null){return;}
	c = getOptCoord(OPT_POS, c);
    }
}
