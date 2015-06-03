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

    private static final Set<String> NOSORT;
    static{
	NOSORT = new HashSet<String>();
	NOSORT.add("Turkey Coop");
	NOSORT.add("Drying Frame");
	NOSORT.add("Cementation Furnace");
	NOSORT.add("Tanning Tub");
    }

    Coord isz;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();
    public int newseq = 0;
    private boolean cansort = false;
    private boolean needSort = true;
    private boolean needresize = false;
    private Coord risz;
    private Set<String> freecells;
    private LinkedList<WItem> items;

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
	risz = isz = sz;
	addsorting();
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
	    resort();
	}
	return(ret);
    }

    public void cdestroy(Widget w) {
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
	    ui.destroy(wmap.remove(i));
	    resort();
	}
	super.cdestroy(w);
    }

    public boolean drop(Coord cc, Coord ul) {
	Coord c = sqroff(ul.add(isqsz.div(2)));
	int i = c.y * isz.x + c.x;
	if(items != null) {
	    if(items.size() > i) {
		c = items.get(i).item.c;
	    } else {
		Iterator<String> iterator = freecells.iterator();
		if(iterator.hasNext()) {
		    String sc = iterator.next();
		    c = new Coord(sc);
		    freecells.remove(sc);
		}
	    }
	}
	wdgmsg("drop", c);
	return (true);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public void uimsg(String msg, Object... args) {
	if(msg.equals("sz")) {
	    risz = isz = (Coord)args[0];
	    if(cansort){
		resort();
	    } else {
		resize(invsz(isz));
	    }
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(msg.equals("transfer-same")){
	    process(getSame((String) args[0],(Boolean)args[1]), "transfer");
	} else if(msg.equals("drop-same")){
	    process(getSame((String) args[0], (Boolean) args[1]), "drop");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    private void process(List<WItem> items, String action) {
	for (WItem item : items){
	    item.item.wdgmsg(action, Coord.z);
	}
    }

    public void sort(boolean value){
	if( value != cansort){
	    cansort = value;
	    if(cansort){
		resort();
	    } else {
		for(WItem item : wmap.values()){
		    item.c = sqoff(item.item.c);
		}
		items = null;
		freecells = null;
		isz = risz;
		resize(invsz(isz));
	    }
	}
    }

    @Override
    public void tick(double dt) {
	if(cansort) {
	    if(needresize) {
		sortedsize();
		resize(invsz(isz));
		needresize = false;
	    }
	    if(needSort) {
		try {
		    items = new LinkedList<WItem>(wmap.values());
		    items.sort(WItemComparator.sort);
		    for(int i = 0; i < items.size(); i++) {
			items.get(i).c = sqoff(new Coord(i % isz.x, i / isz.x));
		    }
		    needSort = false;
		} catch(Loading ignored) {
		}
	    }
	    if(items != null && freecells == null) {
		calcfreecells();
	    }
	}
	super.tick(dt);
    }

    public void resort() {
	needSort = true;
	needresize = true;
	items = null;
	freecells = null;
    }

    private void calcfreecells() {
	freecells = new HashSet<String>(isz.x * isz.y);
	Coord c = new Coord();
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		freecells.add(c.toString());
	    }
	}
	Set<GItem> items = wmap.keySet();
	for(GItem item : items){
	    freecells.remove(item.c.toString());
	}
    }

    private void sortedsize() {
	if(cansort) {
	    if(this.equals(this.ui.gui.maininv)) {
		int n = wmap.size() + 1;
		float aspect_ratio = 1.5f;
		isz = new Coord();
		double a = Math.ceil(aspect_ratio * Math.sqrt(n / aspect_ratio));
		isz.x = (int) Math.max(4, a);
		isz.y = (int) Math.max(4, Math.ceil(n/a));
	    }
	}
    }

    private void addsorting() {
	Window wnd = getparent(Window.class);
	if(wnd != null && !risz.equals(new Coord(1, 1)) && wnd.cap != null && !NOSORT.contains(wnd.cap.text)) {
	    IButton btnsort = new IButton(Coord.z, wnd, Window.obtni[0], Window.obtni[1], Window.obtni[2]) {
		@Override
		public void click() {
		    sort(!cansort);
		}
	    };
	    wnd.addtwdg(btnsort);
	}
    }

    private List<WItem> getSame(String name, Boolean ascending) {
	List<WItem> items = new ArrayList<WItem>();
	for (Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
	    if (wdg.visible && wdg instanceof WItem) {
		if (((WItem) wdg).item.resname().equals(name))
		    items.add((WItem) wdg);
	    }
	}
	Collections.sort(items, ascending? WItemComparator.cmp_stats_asc : WItemComparator.cmp_stats_desc);
	return items;
    }
    
}
