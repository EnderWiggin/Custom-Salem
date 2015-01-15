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

import static haven.Inventory.sqlite;
import static haven.Inventory.sqlo;

public class Equipory extends Widget implements DTarget {
    static Coord ecoords[] = {
	new Coord(250, 0),
	new Coord(50, 70),
	new Coord(250, 70),
	new Coord(300, 70),
	new Coord(50, 0),
	new Coord(50, 210),
	new Coord(25, 140),
	new Coord(275, 140),
	null,
	new Coord(0, 0),
	new Coord(0, 210),
	new Coord(300, 0),
	new Coord(300, 210),
	new Coord(100, 0),
	new Coord(0, 70),
	new Coord(250, 210),
    };
    static Tex ebgs[] = {
	Resource.loadtex("gfx/hud/inv/head"),
	Resource.loadtex("gfx/hud/inv/face"),
	Resource.loadtex("gfx/hud/inv/shirt"),
	Resource.loadtex("gfx/hud/inv/torsoa"),
	Resource.loadtex("gfx/hud/inv/keys"),
	Resource.loadtex("gfx/hud/inv/belt"),
	Resource.loadtex("gfx/hud/inv/lhande"),
	Resource.loadtex("gfx/hud/inv/rhande"),
	null,
	Resource.loadtex("gfx/hud/inv/wallet"),
	Resource.loadtex("gfx/hud/inv/coat"),
	Resource.loadtex("gfx/hud/inv/cape"),
	Resource.loadtex("gfx/hud/inv/pants"),
	null,
	Resource.loadtex("gfx/hud/inv/back"),
	Resource.loadtex("gfx/hud/inv/feet"),
    };
    static Coord isz;
    static {
	isz = new Coord();
	for(Coord ec : ecoords) {
	    if(ec == null)
		continue;
	    if(ec.x + sqlite.sz().x > isz.x)
		isz.x = ec.x + sqlite.sz().x;
	    if(ec.y + sqlite.sz().y > isz.y)
		isz.y = ec.y + sqlite.sz().y;
	}
    }

    private final AttrBonusWdg bonuses;
    WItem[] slots = new WItem[ecoords.length];
    Map<GItem, WItem[]> wmap = new HashMap<GItem, WItem[]>();
    private EquipOpts opts;

    @RName("epry")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    long gobid;
	    if(args.length < 1)
		gobid = parent.getparent(GameUI.class).plid;
	    else
		gobid = (Integer)args[0];
	    return(new Equipory(c, parent, gobid));
	}
    }

    private class Boxen extends Widget {
	private Boxen() {
	    super(Coord.z, isz, Equipory.this);
	}

	public void draw(GOut g) {
	    for(int i = 0; i < ecoords.length; i++) {
		if(ecoords[i] == null)
		    continue;
		g.image(sqlite, ecoords[i]);
		if((slots[i] == null) && (ebgs[i] != null))
		    g.image(ebgs[i], ecoords[i].add(sqlo));
	    }
	}
    }

    public Equipory(Coord c, Widget parent, long gobid) {
	super(c, isz/*.add(175, 0)*/, parent);
	bonuses = new AttrBonusWdg(this, new Coord(isz.x, 0));
	Avaview ava = new Avaview(Coord.z, isz, this, gobid, "equcam") {
		public boolean mousedown(Coord c, int button) {
		    return(false);
		}

		protected java.awt.Color clearcolor() {return(null);}
	    };
	new Boxen();

	opts = new EquipOpts(new Coord(200,100), ui.gui);
	opts.hide();
	Window p = (Window) parent;
	Coord btnc = new Coord(sz.x - Window.cbtni[0].getWidth() - p.cbtn.sz.x - 2, p.cbtn.c.y);
	new IButton(btnc, p, Window.rbtni[0], Window.rbtni[1], Window.rbtni[2]) {
	    public void click() {
		toggleOptions();
	    }
	};
	pack();
    }

    private void toggleOptions() {
	if(opts != null){
	    opts.toggle();
	}
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if (sender  instanceof GItem && wmap.containsKey(sender) && msg.equals("ttupdate")) {
	    bonuses.update(slots);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
	Widget ret = gettype(type).create(Coord.z, this, cargs);
	if(ret instanceof GItem) {
	    GItem g = (GItem)ret;
	    g.sendttupdate = true;
	    WItem[] v = new WItem[pargs.length];
	    for(int i = 0; i < pargs.length; i++) {
		int ep = (Integer)pargs[i];
		slots[ep] = v[i] = new WItem(ecoords[ep].add(sqlo), this, g);
	    }
	    wmap.put(g, v);
	}
	return(ret);
    }
    
    public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
	    for(WItem v : wmap.remove(i)) {
		ui.destroy(v);
		for(int s = 0; s < slots.length; s++) {
		    if(slots[s] == v)
			slots[s] = null;
		}
	    }
	    bonuses.update(slots);
	}
    }
    
    public boolean drop(Coord cc, Coord ul) {
	ul = ul.add(sqlite.sz().div(2));
	for(int i = 0; i < ecoords.length; i++) {
	    if(ecoords[i] == null)
		continue;
	    if(ul.isect(ecoords[i], sqlite.sz())) {
		wdgmsg("drop", i);
		return(true);
	    }
	}
	wdgmsg("drop", -1);
	return(true);
    }
    
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
}
