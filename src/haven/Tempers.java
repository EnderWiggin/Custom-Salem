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

public class Tempers extends Widget {
    public static final Tex bg = Resource.loadtex("gfx/hud/tempers");
    public static final Tex cross = Resource.loadtex("gfx/hud/tempersc");
    public static final Coord mid = new Coord(93, 38);
    static final Color softc = new Color(255, 255, 255, 64);
    static final Color foodc = new Color(255, 255, 0, 64);
    static final int l = 32;
    static final String[] anm = {"blood", "phlegm", "ybile", "bbile"};
    static final String[] rnm = {"Blood", "Phlegm", "Yellow Bile", "Black Bile"};
    static final Color[] cols = {
	new Color(255, 0, 0, 255),
	new Color(255, 255, 255, 255),
	new Color(255, 255, 0, 255),
	new Color(0, 64, 0, 255),
    };
    int[] soft = new int[4], hard = new int[4];
    int[] lmax = new int[4];
    boolean full = false;
    Tex tt = null;
    
    public Tempers(Coord c, Widget parent) {
	super(c, bg.sz(), parent);
    }
    
    private static int dispval(int val, int max) {
	if(val == 0)
	    return(0);
	return(Math.min(Math.max(1, (val * 35) / max), 35));
    }

    public void draw(GOut g) {
	g.image(bg, Coord.z);
	int[] max = new int[4];
	full = true;
	for(int i = 0; i < 4; i++) {
	    max[i] = ui.sess.glob.cattr.get(anm[i]).comp;
	    if(max[i] == 0)
		return;
	    if(hard[i] < max[i])
		full = false;
	    if(max[i] != lmax[i])
		tt = null;
	}
	lmax = max;
	if(ui.lasttip instanceof WItem.ItemTip) {
	    GItem item = ((WItem.ItemTip)ui.lasttip).item();
	    FoodInfo food = GItem.find(FoodInfo.class, item.info());
	    if(food != null) {
		g.chcolor(foodc);
		g.poly(mid.add(0, -dispval(soft[0] + food.tempers[0], max[0])),
		       mid.add(dispval(soft[1] + food.tempers[1], max[1]), 0),
		       mid.add(0, dispval(soft[2] + food.tempers[2], max[2])),
		       mid.add(-dispval(soft[3] + food.tempers[3], max[3]), 0));
	    }
	}
	g.chcolor(softc);
	g.poly(mid.add(0, -dispval(soft[0], max[0])),
	       mid.add(dispval(soft[1], max[1]), 0),
	       mid.add(0, dispval(soft[2], max[2])),
	       mid.add(-dispval(soft[3], max[3]), 0));
	g.chcolor();
	g.poly2(mid.add(0, -dispval(hard[0], max[0])), cols[0],
		mid.add(dispval(hard[1], max[1]), 0), cols[1],
		mid.add(0, dispval(hard[2], max[2])), cols[2],
		mid.add(-dispval(hard[3], max[3]), 0), cols[3]);
	if(full)
	    g.chcolor(64, 255, 192, 255);
	g.aimage(cross, mid, 0.5, 0.5);
	g.chcolor();
    }
    
    public void upds(int[] n) {
	this.soft = n;
	tt = null;
    }
    
    public void updh(int[] n) {
	this.hard = n;
	tt = null;
    }
    
    public Object tooltip(Coord c, boolean again) {
	if(c.dist(mid) < l) {
	    if(tt == null) {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < 4; i++)
		    buf.append(String.format("%s: %s/%s/%s\n", rnm[i], Utils.fpformat(hard[i], 3, 1), Utils.fpformat(soft[i], 3, 1), Utils.fpformat(lmax[i], 3, 1)));
		tt = RichText.render(buf.toString(), 0).tex();
	    }
	    return(tt);
	}
	return(null);
    }

    public boolean mousedown(Coord c, int button) {
	if(c.dist(mid) < l) {
	    getparent(GameUI.class).act("gobble");
	    return(true);
	}
	return(super.mousedown(c, button));
    }
}
