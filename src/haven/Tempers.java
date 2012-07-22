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
import java.awt.font.TextAttribute;

public class Tempers extends Widget {
    public static final Tex bg = Resource.loadtex("gfx/hud/tempers");
    public static final Tex cross = Resource.loadtex("gfx/hud/tempersc");
    public static final Coord mid = new Coord(93, 38);
    private static final int wdiamond = 35;
    private static final int wplain = mid.x*2 - 8;
    private static final Coord plainbg = new Coord(wplain+8, 67);
    static final Color softc = new Color(255, 255, 255, 96);
    static final Color foodc = new Color(255, 255, 0, 96);
    static final int l = 32;
    static final String[] anm = {"blood", "phlegm", "ybile", "bbile"};
    static final String[] rnm = {"Blood", "Phlegm", "Yellow Bile", "Black Bile"};
    static final Color[] cols = {
	new Color(220, 32, 32, 255),
	new Color(255, 255, 255, 255),
	new Color(220, 220, 32, 255),
	new Color(64, 64, 64, 255),
    };
    int[] soft = new int[4], hard = new int[4];
    int[] lmax = new int[4];
    boolean full = false;
    private static int w = wdiamond;
    Tex tt = null;
    Tex[] texts = null;
    private boolean mover = false;
    
    public Tempers(Coord c, Widget parent) {
	super(c, bg.sz(), parent);
    }
    
    private static int dispval(int val, int max) {
	if(val == 0)
	    return(0);
	return(Math.min(Math.max(1, (val * w) / max), w));
    }
    
    private static void bar(GOut g, int value, Coord c){
	g.frect(c, new Coord(value, 14));
    }
    
    private static void bar(GOut g, int value, Coord c, Color col){
	Color cl = g.getcolor();
	g.chcolor(col);
	bar(g, value, c);
	g.chcolor(cl);
    }
    
    public void draw(GOut g) {
	int[] max = new int[4];
//	int[] max = {10,12,13,12};
	full = true;
	for(int i = 0; i < 4; i++) {
	    max[i] = ui.sess.glob.cattr.get(anm[i]).comp;
	    if(max[i] == 0)
		return;
	    if(hard[i] < max[i])
		full = false;
	    if(max[i] != lmax[i]){
		tt = null;
		texts = null;
	    }
	}
	lmax = max;
	if(Config.plain_tempers){
	    draw_plain(g);
	} else {
	    draw_diamond(g);
	}
    }
    
    public void draw_plain(GOut g){
	int step = 15;
	int b = 4;
	Coord c0 = new Coord(4,b);
	w = wplain;
	
	if(full){
	    g.chcolor(64, 255, 192, 255);
	} else {
	    g.chcolor(64, 96, 128, 222);
	}
	g.frect(Coord.z, plainbg);
	
	if(ui.lasttip instanceof WItem.ItemTip) {
	    GItem item = ((WItem.ItemTip)ui.lasttip).item();
	    FoodInfo food = ItemInfo.find(FoodInfo.class, item.info());
	    if(food != null) {
		g.chcolor(foodc);
		bar(g, dispval(soft[0] + food.tempers[0], lmax[0]), c0);
		c0.y += step;
		bar(g, dispval(soft[1] + food.tempers[1], lmax[1]), c0);
		c0.y += step;
		bar(g, dispval(soft[2] + food.tempers[2], lmax[2]), c0);
		c0.y += step;
		bar(g, dispval(soft[3] + food.tempers[3], lmax[3]), c0);
	    }
	}
	g.chcolor(softc);
	c0.y = b;
	bar(g, dispval(soft[0], lmax[0]), c0);
	c0.y += step;
	bar(g, dispval(soft[1], lmax[1]), c0);
	c0.y += step;
	bar(g, dispval(soft[2], lmax[2]), c0);
	c0.y += step;
	bar(g, dispval(soft[3], lmax[3]), c0);
	g.chcolor();
	
	c0.y = b;
	bar(g, dispval(hard[0], lmax[0]), c0, cols[0]);
	c0.y += step;
	bar(g, dispval(hard[1], lmax[1]), c0, cols[1]);
	c0.y += step;
	bar(g, dispval(hard[2], lmax[2]), c0, cols[2]);
	c0.y += step;
	bar(g, dispval(hard[3], lmax[3]), c0, cols[3]);
	
	if(mover || Config.show_tempers){
	    if(texts == null){
		texts = new Tex[4];
		for(int i = 0; i < 4; i++){
		    String str = String.format("%s / %s / %s", Utils.fpformat(hard[i], 3, 1), Utils.fpformat(soft[i], 3, 1), Utils.fpformat(lmax[i], 3, 1));
//		    texts[i] = new TexI(RichText.render(str, 0, TextAttribute.FOREGROUND, new Color(32,32,64), TextAttribute.SIZE, 12).img);
		    texts[i] = new TexI(Utils.outline2(RichText.render(str, 0, TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, TextAttribute.FOREGROUND, new Color(32,32,64), TextAttribute.SIZE, 12).img, new Color(220, 220, 220), false));
		}
	    }

	    c0.x = mid.x;
	    c0.y = 10;
	    for(int i = 0; i<4; i++){
		g.aimage(texts[i], c0, 0.5, 0.5);
		c0.y += step;
	    }
	}
	
	g.chcolor();
    }
    
    public void draw_diamond(GOut g){
	g.image(bg, Coord.z);
	w = wdiamond;
	if(ui.lasttip instanceof WItem.ItemTip) {
	    GItem item = ((WItem.ItemTip)ui.lasttip).item();
	    FoodInfo food = ItemInfo.find(FoodInfo.class, item.info());
	    if(food != null) {
		g.chcolor(foodc);
		g.poly(mid.add(0, -dispval(soft[0] + food.tempers[0], lmax[0])),
		       mid.add(dispval(soft[1] + food.tempers[1], lmax[1]), 0),
		       mid.add(0, dispval(soft[2] + food.tempers[2], lmax[2])),
		       mid.add(-dispval(soft[3] + food.tempers[3], lmax[3]), 0));
	    }
	}
	g.chcolor(softc);
	g.poly(mid.add(0, -dispval(soft[0], lmax[0])),
	       mid.add(dispval(soft[1], lmax[1]), 0),
	       mid.add(0, dispval(soft[2], lmax[2])),
	       mid.add(-dispval(soft[3], lmax[3]), 0));
	g.chcolor();
	g.poly2(mid.add(0, -dispval(hard[0], lmax[0])), cols[0],
		mid.add(dispval(hard[1], lmax[1]), 0), cols[1],
		mid.add(0, dispval(hard[2], lmax[2])), cols[2],
		mid.add(-dispval(hard[3], lmax[3]), 0), cols[3]);
	if(full)
	    g.chcolor(64, 255, 192, 255);
	g.aimage(cross, mid, 0.5, 0.5);
	g.chcolor();
    }
    
    public void upds(int[] n) {
	this.soft = n;
	tt = null;
	texts = null;
    }
    
    public void updh(int[] n) {
	this.hard = n;
	tt = null;
	texts = null;
    }
    
    
    
    public Object tooltip(Coord c, boolean again) {
	if(!Config.plain_tempers && (c.dist(mid) < l)) {
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
	if(Config.plain_tempers && button == 3){
	    if(OptWnd.instance != null){
		OptWnd.instance.opt_show_tempers.set(!Config.show_tempers);
	    } else {
		Config.show_tempers = !Config.show_tempers;
		Utils.setprefb("show_tempers", Config.show_tempers);
	    }
	    return false;
	}
	
	if(Config.plain_tempers || (c.dist(mid) < l)) {
	    getparent(GameUI.class).act("gobble");
	    return(true);
	}
	return(super.mousedown(c, button));
    }

    @Override
    public void mousemove(Coord c) {
	if(Config.plain_tempers){
	    mover = c.isect(Coord.z, plainbg);
	}
	super.mousemove(c);
    }
}
