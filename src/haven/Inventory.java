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

public class Inventory extends Widget implements DTarget {
    private static final Tex obt = Resource.loadtex("gfx/hud/inv/obt");
    private static final Tex obr = Resource.loadtex("gfx/hud/inv/obr");
    private static final Tex obb = Resource.loadtex("gfx/hud/inv/obb");
    private static final Tex obl = Resource.loadtex("gfx/hud/inv/obl");
    private static final Tex ctl = Resource.loadtex("gfx/hud/inv/octl");
    private static final Tex ctr = Resource.loadtex("gfx/hud/inv/octr");
    private static final Tex cbr = Resource.loadtex("gfx/hud/inv/ocbr");
    private static final Tex cbl = Resource.loadtex("gfx/hud/inv/ocbl");
    private static final Tex bsq = Resource.loadtex("gfx/hud/inv/sq");
    public static final Coord sqsz = bsq.sz();
    public static final Coord isqsz = new Coord(40, 40);
    public static final Tex sqlite = Resource.loadtex("gfx/hud/inv/sq1");
    public static final Coord sqlo = new Coord(4, 4);
    public static final Tex refl = Resource.loadtex("gfx/hud/invref");
    Coord isz;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();
    public int newseq = 0;

    @RName("inv")
    public static class $_ implements Factory {
	public Widget create(Coord c, Widget parent, Object[] args) {
	    return(new Inventory(c, (Coord)args[0], parent));
	}
    }

    public void draw(GOut g) {
	invsq(g, Coord.z, isz);
	for(Coord cc = new Coord(0, 0); cc.y < isz.y; cc.y++) {
	    for(cc.x = 0; cc.x < isz.x; cc.x++) {
		invrefl(g, sqoff(cc), isqsz);
	    }
	}
	super.draw(g);
    }

    public Inventory(Coord c, Coord sz, Widget parent) {
	super(c, invsz(sz), parent);
	isz = sz;
    }

    public static Coord sqoff(Coord c) {
	return(c.mul(sqsz).add(ctl.sz()));
    }

    public static Coord sqroff(Coord c) {
	return(c.sub(ctl.sz()).div(sqsz));
    }

    public static Coord invsz(Coord sz) {
	return(sz.mul(sqsz).add(ctl.sz()).add(cbr.sz()).sub(4, 4));
    }

    public static void invrefl(GOut g, Coord c, Coord sz) {
	Coord ul = g.ul.sub(g.ul.div(2)).mod(refl.sz()).inv();
	Coord rc = new Coord();
	for(rc.y = ul.y; rc.y < c.y + sz.y; rc.y += refl.sz().y) {
	    for(rc.x = ul.x; rc.x < c.x + sz.x; rc.x += refl.sz().x) {
		g.image(refl, rc, c, sz);
	    }
	}
    }

    public static void invsq(GOut g, Coord c, Coord sz) {
	for(Coord cc = new Coord(0, 0); cc.y < sz.y; cc.y++) {
	    for(cc.x = 0; cc.x < sz.x; cc.x++) {
		g.image(bsq, c.add(cc.mul(sqsz)).add(ctl.sz()));
	    }
	}
	for(int x = 0; x < sz.x; x++) {
	    g.image(obt, c.add(ctl.sz().x + sqsz.x * x, 0));
	    g.image(obb, c.add(ctl.sz().x + sqsz.x * x, obt.sz().y + (sqsz.y * sz.y) - 4));
	}
	for(int y = 0; y < sz.y; y++) {
	    g.image(obl, c.add(0, ctl.sz().y + sqsz.y * y));
	    g.image(obr, c.add(obl.sz().x + (sqsz.x * sz.x) - 4, ctl.sz().y + sqsz.y * y));
	}
	g.image(ctl, c);
	g.image(ctr, c.add(ctl.sz().x + (sqsz.x * sz.x) - 4, 0));
	g.image(cbl, c.add(0, ctl.sz().y + (sqsz.y * sz.y) - 4));
	g.image(cbr, c.add(cbl.sz().x + (sqsz.x * sz.x) - 4, ctr.sz().y + (sqsz.y * sz.y) - 4));
    }

    public static void invsq(GOut g, Coord c) {
	g.image(sqlite, c);
    }

    public boolean mousewheel(Coord c, int amount) {
	if(ui.modshift) {
	    wdgmsg("xfer", amount);
	}
	return(true);
    }

    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
	Coord c = (Coord)pargs[0];
	Widget ret = gettype(type).create(c, this, cargs);
	if(ret instanceof GItem) {
	    GItem i = (GItem)ret;
	    wmap.put(i, new WItem(sqoff(c), this, i));
	    newseq++;
	}
	return(ret);
    }

    public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
	    ui.destroy(wmap.remove(i));
	}
    }

    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", sqroff(ul.add(isqsz.div(2))));
	return(true);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "sz") {
	    isz = (Coord)args[0];
	    resize(invsz(isz));
	}
    }
}
