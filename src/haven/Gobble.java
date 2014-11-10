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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.*;
import static haven.PUtils.*;
import static haven.Tempers.*;

public class Gobble extends SIWidget {
    public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/gbg");
    static Text.Foundry tnf = new Text.Foundry(new java.awt.Font("serif", java.awt.Font.BOLD, 16)).aa(true);
    public int[] lev = new int[4];
    public List<TypeMod> mods = new ArrayList<TypeMod>();
    static final Color loc = new Color(0, 128, 255);
    static final Color hic = new Color(0, 128, 64);
    static final BufferedImage[] lobars, hibars;
    private boolean updt = true;
    private TypeList typelist;
    private int[] lmax = new int[4];
    private int max;
    private Tex lvlmask;
    private long lvltime;
    Tex[] texts = null;
    private Tex levels;

    static {
	int n = bars.length;
	BufferedImage[] l = new BufferedImage[n];
	BufferedImage[] h = new BufferedImage[n];
	for(int i = 0; i < n; i++) {
	    l[i] = monochromize(bars[i], loc);
	    h[i] = monochromize(bars[i], hic);
	}
	lobars = l;
	hibars = h;
    }

    public static class TypeMod {
	public final Indir<Resource> t;
	public double a;
	private Tex rn, rh, ra;
	public TypeMod(Indir<Resource> t, double a) {this.t = t; this.a = a;}
    }

    private class TypeList extends Widget {
	private int nw;

	private TypeList(Coord c, Widget parent) {
	    super(c, Coord.z, parent);
	}

	public void tick(double dt) {
	    if(updt) {
		nw = 0;
		int aw = 0;
		for(TypeMod m : mods) {
		    if(m.rn == null) {
			try {
			    BufferedImage img = m.t.get().layer(Resource.imgc).img;
			    String nm = m.t.get().layer(Resource.tooltip).t;
			    Text rt = tnf.render(nm);
			    int h = Inventory.sqsz.y;
			    BufferedImage buf = TexI.mkbuf(new Coord(img.getWidth() + 10 + rt.sz().x, h));
			    Graphics g = buf.getGraphics();
			    g.drawImage(img, 0, (h - img.getHeight()) / 2, null);
			    g.drawImage(rt.img, img.getWidth() + 10, (h - rt.sz().y) / 2, null);
			    g.dispose();
			    m.rn = new TexI(rasterimg(blurmask2(buf.getRaster(), 2, 1, new Color(32, 0, 0))));
			    m.rh = new TexI(rasterimg(blurmask2(buf.getRaster(), 2, 1, new Color(192, 128, 0))));
			} catch(Loading l) {
			}
		    }
		    if(m.ra == null) {
			Text rt = tnf.render((int)Math.round(m.a * 100) + "%", new Color(255, (int)(255 * m.a), (int)(255 * m.a)));
			m.ra = new TexI(rasterimg(blurmask2(rt.img.getRaster(), 2, 1, new Color(0, 0, 0))));
		    }
		    nw = Math.max(nw, m.rn.sz().x);
		    aw = Math.max(aw, m.ra.sz().x);
		}
		int h = (Inventory.sqsz.y + 5) * mods.size();
		h += levels.sz().y + 20;
		resize(new Coord(Math.max(nw + 20 + aw, boxsz.x), h));
		this.c = Gobble.this.parentpos(parent).add(boxc).add(0, boxsz.y + 5);
		updt = false;
	    }
	}
	
	public void draw(GOut g) {
	    int tn = 0;
	    int y = 0;
	    int h = Inventory.sqsz.y;
	    boolean[] hl = new boolean[mods.size()];
	    if(lfood != null) {
		for(int t : lfood.types)
		    hl[t] = true;
	    }
	    g.aimage(levels, new Coord(sz.x / 2, y), 0.5, 0);
	    y += levels.sz().y + 20;
	    for(TypeMod m : mods) {
		if(m.rn != null)
		    g.image(hl[tn]?m.rh:m.rn, new Coord(0, y));
		if(m.ra != null)
		    g.image(m.ra, new Coord(nw + 20, y + ((h - m.ra.sz().y) / 2)));
		tn++;
		y += h + 5;
	    }
	}
    }

    public Gobble(Coord c, Widget parent) {
	super(c, Utils.imgsz(Tempers.bg), parent);
	lcount(0, Color.WHITE);
	typelist = new TypeList(Coord.z, getparent(GameUI.class));
    }

    public void destroy() {
	typelist.destroy();
	super.destroy();
    }

    private GobbleInfo lfood;
    public void tick(double dt) {
	int max = 0;
	int[] lmax = new int[4];
	for(int i = 0; i < 4; i++) {
	    lmax[i] = ui.sess.glob.cattr.get(Tempers.anm[i]).base;
	    if(lmax[i] == 0)
		return;
	    if(lmax[i] != this.lmax[i]){
		redraw();
		if(this.lmax[i] != 0){
		    ui.message(String.format("You have raised %s!", Tempers.rnm[i]), GameUI.MsgType.GOOD);
		}
	    }
	    max = Math.max(max, lmax[i]);
	}
	this.lmax = lmax;
	this.max = max;

	GobbleInfo food = null;
	Alchemy ch = null;
	if(ui.lasttip instanceof WItem.ItemTip) {
	    try {
		food = ItemInfo.find(GobbleInfo.class, ((WItem.ItemTip)ui.lasttip).item().info());
		ch = ItemInfo.find(Alchemy.class, ((WItem.ItemTip)ui.lasttip).item().info());
	    } catch(Loading e) {}
	}
	if(lfood != food) {
	    lfood = food;
	    redraw();
	}
    }

    private double foodeff(GobbleInfo food) {
	double ret = 1.0;
	for(int t : lfood.types)
	    ret *= mods.get(t).a;
	return(ret);
    }

    private WritableRaster rgmeter(GobbleInfo food, double e, int t) {
	return(alphablit(rmeter(hibars[t].getRaster(), lev[t] + (int)(e * food.h[t]), max),
			 rmeter(lobars[t].getRaster(), lev[t] + (int)(e * food.l[t]), max),
			 Coord.z));
    }

    private WritableRaster lgmeter(GobbleInfo food, double e, int t) {
	return(alphablit(lmeter(hibars[t].getRaster(), lev[t] + (int)(e * food.h[t]), max),
			 lmeter(lobars[t].getRaster(), lev[t] + (int)(e * food.l[t]), max),
			 Coord.z));
    }

    public void draw(BufferedImage buf) {
	WritableRaster dst = buf.getRaster();
	blit(dst, bg.getRaster(), Coord.z);

	alphablit(dst, rmeter(sbars[0].getRaster(), lmax[0], max), mc[0]);
	alphablit(dst, lmeter(sbars[1].getRaster(), lmax[1], max), mc[1].sub(bars[1].getWidth() - 1, 0));
	alphablit(dst, lmeter(sbars[2].getRaster(), lmax[2], max), mc[2].sub(bars[2].getWidth() - 1, 0));
	alphablit(dst, rmeter(sbars[3].getRaster(), lmax[3], max), mc[3]);

	if(lfood != null) {
	    double e = foodeff(lfood);
	    alphablit(dst, rgmeter(lfood, e, 0), mc[0]);
	    alphablit(dst, lgmeter(lfood, e, 1), mc[1].sub(bars[1].getWidth() - 1, 0));
	    alphablit(dst, lgmeter(lfood, e, 2), mc[2].sub(bars[1].getWidth() - 1, 0));
	    alphablit(dst, rgmeter(lfood, e, 3), mc[3]);
	}

	alphablit(dst, rmeter(bars[0].getRaster(), lev[0], max), mc[0]);
	alphablit(dst, lmeter(bars[1].getRaster(), lev[1], max), mc[1].sub(bars[1].getWidth() - 1, 0));
	alphablit(dst, lmeter(bars[2].getRaster(), lev[2], max), mc[2].sub(bars[2].getWidth() - 1, 0));
	alphablit(dst, rmeter(bars[3].getRaster(), lev[3], max), mc[3]);

	StringBuilder tbuf = new StringBuilder();
	for(int i = 0; i < 4; i++)
	    tbuf.append(String.format("%s: %s/%s\n", rnm[i], Utils.fpformat(lev[i], 3, 1), Utils.fpformat(lmax[i], 3, 1)));
	tooltip = RichText.render(tbuf.toString(), 0).tex();
    }

    public void draw(GOut g) {
	super.draw(g);
	if(lvlmask != null) {
	    long now = System.currentTimeMillis();
	    if(now - lvltime > 1000) {
		lvlmask.dispose();
		lvlmask = null;
	    } else {
		g.chcolor(255, 255, 255, 255 - (int)((255 * (now - lvltime)) / 1000));
		g.image(lvlmask, Coord.z);
	    }
	}
	
	if(Config.show_tempers){
	    int i;
	    if(texts == null){
		texts = new TexI[4];
		for(i = 0; i < 4; i++){
		    int attr = ui.sess.glob.cattr.get(Tempers.anm[i]).comp;
		    String str = String.format("%s / %s (%s)", Utils.fpformat(lev[i], 3, 1), Utils.fpformat(max, 3, 1), Utils.fpformat(attr, 3, 1));
		    texts[i] = text(str);
		}
	    }
	    g.aimage(texts[0], mc[0].add(bars[0].getWidth()/2, bars[0].getHeight()/2 - 1), 0.5, 0.5);
	    g.aimage(texts[1], mc[1].add(-bars[1].getWidth()/2, bars[1].getHeight()/2 - 1), 0.5, 0.5);
	    g.aimage(texts[2], mc[2].add(-bars[2].getWidth()/2, bars[2].getHeight()/2 - 1), 0.5, 0.5);
	    g.aimage(texts[3], mc[3].add(bars[3].getWidth()/2, bars[3].getHeight()/2 - 1), 0.5, 0.5);
	}
    }

    public void updt(int[] n) {
	this.lev = n;
	texts = null;
	redraw();
    }

    public void lvlup(int a) {
	WritableRaster buf = imgraster(imgsz(bg));
	if((a == 0) || (a == 3))
	    alphablit(buf, rmeter(bars[a].getRaster(), 1, 1), mc[a]);
	else
	    alphablit(buf, lmeter(bars[a].getRaster(), 1, 1), mc[a].sub(bars[a].getWidth() - 1, 0));
	imgblur(buf, 2, 2);
	lvlmask = new TexI(rasterimg(buf));
	lvltime = System.currentTimeMillis();
    }

    public void lcount(int n, Color c) {
	Text rt = tnf.render(String.format("Gobble Points: %d", n), c);
	levels = new TexI(rasterimg(blurmask2(rt.img.getRaster(), 2, 1, new Color(0, 0, 0))));
    }

    public void typemod(Indir<Resource> t, double a) {
	updt = true;
	for(TypeMod m : mods) {
	    if(m.t == t) {
		m.a = a;
		m.ra = null;
		return;
	    }
	}
	mods.add(new TypeMod(t, a));
    }

    public Object tooltip(Coord c, Widget prev) {
	if(!c.isect(boxc, boxsz))
	    return(null);
	return(super.tooltip(c, prev));
    }
}
