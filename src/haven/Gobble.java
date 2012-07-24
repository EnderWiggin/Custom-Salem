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

import static haven.Tempers.bar;
import static haven.Tempers.bgc;
import static haven.Tempers.cols;
import static haven.Tempers.softc;
import static haven.Tempers.dispval;
import static haven.Tempers.mid;
import static haven.Tempers.wdiamond;
import static haven.Tempers.wplain;

import java.awt.Color;
import java.awt.Font;

public class Gobble extends Widget {
    static final Tex[] trigi = new Tex[4];
    static final int l = 32;
    static final Coord plainbg = Tempers.plainbg.add(0, 15);
    static final Coord varsz = new Coord(40, 40);
    static final Text.Foundry vf = new Text.Foundry(Font.SERIF, 20);
    public int[] lev = new int[4];
    Tex[] texts = null;
    public Tex var = Tempers.text("Variance: 0");
    Tex ctrig = null;
    long trigt = 0;
    private boolean mover;
    private int lmax[] = {0, 0, 0, 0};
    private static int w = wdiamond;
    static {
	Text.Foundry f = new Text.Foundry("Serif", 30);
	f.aa = true;
	for(int i = 0; i < 4; i++)
	    trigi[i] = f.render(Integer.toString(i + 1), Color.YELLOW).tex();
    }
    
    public Gobble(Coord c, Widget parent) {
	super(c, Tempers.bg.sz(), parent);
    }
    
    public void draw(GOut g) {
	int[] attr = new int[4];
	int max = 0;
	for(int i = 0; i < 4; i++) {
	    attr[i] = ui.sess.glob.cattr.get(Tempers.anm[i]).comp;
	    if(attr[i] == 0)
		return;
	    if(attr[i] > max)
		max = attr[i];
	    if(attr[i] > lmax[i]){
		if(lmax[i] != 0)
		    ui.message(String.format("You have raised %s!", Tempers.rnm[i]));
		lmax[i] = attr[i];
	    }
	}
	
	if(Config.plain_tempers){
	    draw_plain(g, attr, max);
	} else {
	    draw_diamond(g, attr, max);
	}
    }
    
    private void draw_plain(GOut g, int[] attr, int max) {
	int step = 15;
	int b = 4;
	Coord c0 = new Coord(4,b);
	w = wplain;
	
	g.chcolor(bgc);
	g.frect(Coord.z, plainbg);
	g.chcolor();
	
	c0.y = b;
	for(int i = 0; i<4; i++){
	    bar(g, w, c0, softc);
	    bar(g, dispval(lev[i], max), c0, cols[i]);
	    c0.y += step;
	}
	
	if(mover || Config.show_tempers){
	    if(texts == null){
		texts = new Tex[4];
		for(int i = 0; i < 4; i++){
		    String str = String.format("%s / %s (%s)", Utils.fpformat(lev[i], 3, 1), Utils.fpformat(max, 3, 1), Utils.fpformat(attr[i], 3, 1));
		    texts[i] = Tempers.text(str);
		}
	    }

	    c0.x = mid.x;
	    c0.y = 10;
	    for(int i = 0; i<4; i++){
		g.aimage(texts[i], c0, 0.5, 0.5);
		c0.y += step;
	    }
	    g.aimage(var, c0, 0.5, 0.5);
	}
    }
    
    private void draw_diamond(GOut g, int[] attr, int max) {
	g.image(Tempers.bg, Coord.z);
	g.chcolor(softc);
	g.poly(mid.add(0, -((attr[0] * 35) / max)),
	       mid.add(((attr[1] * 35) / max), 0),
	       mid.add(0, ((attr[2] * 35) / max)),
	       mid.add(-((attr[3] * 35) / max), 0));
	g.chcolor();
	double[] cur = new double[4];
	for(int i = 0; i < 4; i++) {
	    cur[i] = (double)lev[i] / (double)max;
	    if(cur[i] < 0.1)
		cur[i] = 0.1;
	}
	g.poly2(mid.add(0, -(int)(cur[0] * 35)), cols[0],
		mid.add((int)(cur[1] * 35), 0), cols[1],
		mid.add(0, (int)(cur[2] * 35)), cols[2],
		mid.add(-(int)(cur[3] * 35), 0), cols[3]);
	if(ctrig != null) {
	    long now = System.currentTimeMillis();
	    if(now - trigt > 1000) {
		ctrig = null;
	    } else {
		g.chcolor(255, 255, 255, (int)((255 * (1000 - (now - trigt))) / 1000));
		g.aimage(ctrig, mid, 0.5, 0.5);
		g.chcolor();
	    }
	}
	g.aimage(Tempers.cross, mid, 0.5, 0.5);
	g.aimage(var, mid.add(35, 0), 0, 0.5);
    }
    
    public void updt(int[] n) {
	this.lev = n;
	texts = null;
    }
    
    public void trig(int a) {
	ctrig = trigi[a];
	trigt = System.currentTimeMillis();
    }
    
    public void updv(int v) {
	if(Config.plain_tempers){
	    this.var = Tempers.text(String.format("Variance: %d",v));
	} else {
	    this.var = vf.render(Integer.toString(v)).tex();
	}
    }

    @Override
    public void mousemove(Coord c) {
	if(Config.plain_tempers){
	    mover = c.isect(Coord.z, plainbg);
	}
	super.mousemove(c);
    }
}
