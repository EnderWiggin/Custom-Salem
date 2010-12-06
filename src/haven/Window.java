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
import java.awt.image.BufferedImage;

public class Window extends Widget implements DTarget {
    static Tex bg = Resource.loadtex("gfx/hud/bgtex");
    static Tex topblob = Resource.loadtex("gfx/hud/tmain");
    static Tex botblob = Resource.loadtex("gfx/hud/bmain");
    static BufferedImage[] cbtni = new BufferedImage[] {
	Resource.loadimg("gfx/hud/cbtn"),
	Resource.loadimg("gfx/hud/cbtnd"),
	Resource.loadimg("gfx/hud/cbtnh")}; 
    static Color cc = Color.YELLOW;
    static Text.Foundry cf = new Text.Foundry(new Font("Serif", Font.PLAIN, 12));
    public static final IBox swbox = new IBox("gfx/hud", "stl", "str", "sbl", "sbr", "sextv", "sextv", "sexth", "sexth");
    private static final IBox wbox = new IBox("gfx/hud", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb");
    boolean dt = false;
    Text cap;
    boolean dm = false;
    public Coord atl, asz, wsz;
    public Coord tlo, rbo;
    public Coord mrgn = new Coord(20, 20);
    public Coord doff;
    public IButton cbtn;
	
    static {
	Widget.addtype("wnd", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    if(args.length < 2)
			return(new Window(c, (Coord)args[0], parent, null));
		    else
			return(new Window(c, (Coord)args[0], parent, (String)args[1]));
		}
	    });
    }

    private void placecbtn() {
	cbtn.c = xlate(new Coord(tlo.x + wsz.x - Utils.imgsz(cbtni[0]).x, tlo.y), false);
    }
	
    public Window(Coord c, Coord sz, Widget parent, String cap, Coord tlo, Coord rbo) {
	super(c, new Coord(0, 0), parent);
	this.tlo = tlo;
	this.rbo = rbo;
	cbtn = new IButton(Coord.z, this, cbtni[0], cbtni[1], cbtni[2]);
	if(cap != null)
	    this.cap = cf.render(cap, cc);
	sz = sz.add(tlo).add(rbo).add(wbox.bisz()).add(mrgn.mul(2));
	this.sz = sz;
	atl = wbox.tloff().add(tlo);
	wsz = sz.sub(tlo).sub(rbo);
	asz = wsz.sub(wbox.bisz()).sub(mrgn.mul(2));
	placecbtn();
	setfocustab(true);
	parent.setfocus(this);
    }
	
    public Window(Coord c, Coord sz, Widget parent, String cap) {
	this(c, sz, parent, cap, new Coord(0, 0), new Coord(0, 0));
    }
	
    public void cdraw(GOut g) {
    }
	
    public void draw(GOut og) {
	GOut g = og.reclip(tlo, wsz);
	Coord bgul = wbox.tloff();
	Coord bgsz = wsz.sub(wbox.bisz());
	Coord bgc = new Coord();
	for(bgc.y = 0; bgc.y < bgsz.y; bgc.y += bg.sz().y) {
	    for(bgc.x = 0; bgc.x < bgsz.x; bgc.x += bg.sz().x)
		g.image(bg, bgc.add(bgul), bgul, bgsz);
	}
	cdraw(og.reclip(xlate(Coord.z, true), sz));
	wbox.draw(g, Coord.z, wsz);
	if(wsz.x > wbox.ctl.sz().x + wbox.ctr.sz().x + 0 + topblob.sz().x) {
	    g.image(topblob, new Coord((wsz.x - topblob.sz().x) / 2, 0));
	    g.image(botblob, new Coord((wsz.x - botblob.sz().x) / 2, wsz.y - botblob.sz().y));
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
	super.draw(og);
    }
	
    public void pack() {
	Coord max = new Coord(0, 0);
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if(wdg == cbtn)
		continue;
	    Coord br = wdg.c.add(wdg.sz);
	    if(br.x > max.x)
		max.x = br.x;
	    if(br.y > max.y)
		max.y = br.y;
	}
	resize(max.sub(1, 1));
    }
    
    public void resize(Coord sz) {
	sz = sz.add(tlo).add(rbo).add(wbox.bisz()).add(mrgn.mul(2));
	this.sz = sz;
	wsz = sz.sub(tlo).sub(rbo);
	asz = wsz.sub(wbox.bisz()).sub(mrgn.mul(2));
	placecbtn();
	for(Widget ch = child; ch != null; ch = ch.next)
	    ch.presize();
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
	
    public Coord xlate(Coord c, boolean in) {
	Coord ctl = wbox.tloff();
	if(in)
	    return(c.add(ctl).add(tlo).add(mrgn));
	else
	    return(c.add(ctl.inv()).add(tlo.inv()).add(mrgn.inv()));
    }
	
    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	raise();
	if(super.mousedown(c, button))
	    return(true);
	if(!c.isect(tlo, sz.add(tlo.inv()).add(rbo.inv())))
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
	    ui.grabmouse(null);
	    dm = false;
	} else {
	    super.mouseup(c, button);
	}
	return(true);
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
	    wdgmsg("close");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
	
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    wdgmsg("close");
	    return(true);
	}
	return(super.type(key, ev));
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
    
    public Object tooltip(Coord c, boolean again) {
	Object ret = super.tooltip(c, again);
	if(ret != null)
	    return(ret);
	else
	    return("");
    }
}
