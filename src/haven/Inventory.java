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
    public static final Tex invsq = Resource.loadtex("gfx/hud/invsq");
    public static final Tex refl = Resource.loadtex("gfx/hud/invref");
    public static final Coord sqsz = new Coord(33, 33);
    Coord isz;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();

    static {
	Widget.addtype("inv", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    return(new Inventory(c, (Coord)args[0], parent));
		}
	    });
    }

    public void draw(GOut g) {
	Coord c = new Coord();
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		invsq(g, c.mul(sqsz));
	    }
	}
	super.draw(g);
    }
	
    public Inventory(Coord c, Coord sz, Widget parent) {
	super(c, sqsz.mul(sz).add(1,1), parent);
	isz = sz;
    }
    
    public static void invsq(GOut g, Coord c) {
	g.image(invsq, c);
	Coord ul = g.ul.sub(g.ul.div(2)).mod(refl.sz()).inv();
	Coord rc = new Coord();
	for(rc.y = ul.y; rc.y < c.y + invsq.sz().y; rc.y += refl.sz().y) {
	    for(rc.x = ul.x; rc.x < c.x + invsq.sz().x; rc.x += refl.sz().x) {
		g.image(refl, rc, c.add(1, 1), invsq.sz().sub(2, 2));
	    }
	}
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
//	if(button == 2){
//	    int i = 0;
//	    Coord ct = new Coord();
//	    List<GItem> items = getAll();
//	    Collections.sort(items, GItem.comp);
//	    for(GItem item : items){
//		item.wdgmsg("take", Coord.z);
//		ct.x = i%isz.x;
//		ct.y = i/isz.x;
//		wdgmsg("drop", ct);
//		i++;
//	    }
//	    return true;
//	}
	return super.mousedown(c, button);
    }

    public boolean mousewheel(Coord c, int amount) {
	if(amount < 0)
	    wdgmsg("xfer", -1, ui.modflags());
	if(amount > 0)
	    wdgmsg("xfer", 1, ui.modflags());
	return(true);
    }
    
    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
	Coord c = (Coord)pargs[0];
	Widget ret = gettype(type).create(c, this, cargs);
	if(ret instanceof GItem) {
	    GItem i = (GItem)ret;
	    wmap.put(i, new WItem(c.mul(sqsz).add(1, 1), this, i));
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
	wdgmsg("drop", tilify(ul));
	return(true);
    }

    private static Coord tilify(Coord c){
	return c.add(sqsz.div(2)).div(invsq.sz());
    }
    
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "sz") {
	    isz = (Coord)args[0];
	    sz = invsq.sz().add(new Coord(-1, -1)).mul(isz).add(new Coord(1, 1));
	}
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(msg.equals("transfer-same")){
	    process(getSame((String) args[0]), "transfer");
	} else if(msg.equals("drop-same")){
	    process(getSame((String) args[0]), "drop");
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    private void process(List<GItem> items, String action) {
	for (GItem item : items){
	    item.wdgmsg(action, Coord.z);
	}
    }

    private List<GItem> getSame(String name) {
	List<GItem> items = new ArrayList<GItem>();
	for (Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
	    if (wdg.visible && wdg instanceof WItem) {
		if (((WItem) wdg).item.resname().equals(name))
		    items.add(((WItem) wdg).item);
	    }
	}
	return items;
    }
    
    private List<GItem> getAll() {
	List<GItem> items = new ArrayList<GItem>();
	for (Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
	    if (wdg.visible && wdg instanceof WItem) {
		items.add(((WItem) wdg).item);
	    }
	}
	return items;
    }
}
