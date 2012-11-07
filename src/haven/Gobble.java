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
import java.awt.Graphics;
import java.awt.image.*;
import static haven.PUtils.*;
import static haven.Tempers.*;

public class Gobble extends SIWidget {
    public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/gbg");
    static final Text.Foundry vf = new Text.Foundry("Serif", 20).aa(true);
    public int[] lev = new int[4];
    public int var;
    public Event[] cevs = null;
    public Event[] ttevs = null;
    public Progress prog;
    private int[] lmax = new int[4];
    private int max;
    private Tex lvlmask;
    private long lvltime;
    Tex[] texts = null;

    public static class Event extends SIWidget {
	public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/epbg");
	public static final BufferedImage cap = Resource.loadimg("gfx/hud/tempers/epmask");
	public static final BufferedImage chm = Resource.loadimg("gfx/hud/tempers/epchm");
	public static final BufferedImage[] bars;
	public static final BufferedImage[] titles;
	public static final Coord[] mc = {new Coord(28, 35), new Coord(28, 59), new Coord(28, 83), new Coord(28, 107)};
	public static final Coord chmc = new Coord(33, 124);
	public static final Text.Furnace chmf = new Text.Imager(new Text.Foundry(new Font("Serif", Font.BOLD, 13), Color.WHITE).aa(true)) {
		protected BufferedImage proc(Text text) {
		    return(rasterimg(blurmask2(text.img.getRaster(), 1, 1, Color.BLACK)));
		}
	    };
	public final int ch, prob;
	public final int[] acc;
	public final int max;
	public int alpha = 255;

	static {
	    int n = anm.length;
	    BufferedImage[] b = new BufferedImage[n];
	    for(int i = 0; i < n; i++)
		b[i] = Resource.loadimg("gfx/hud/tempers/ep-" + anm[i]);
	    bars = b;
	    int an = Alchemy.names.length;
	    BufferedImage[] t = new BufferedImage[an];
	    Text.Foundry fnd = new Text.Foundry(new Font("Serif", Font.BOLD, 14)).aa(true);
	    for(int i = 0; i < n; i++) {
		t[i] = fnd.render(Alchemy.names[i], Alchemy.colors[i]).img;
	    }
	    titles = t;
	}

	public Event(Coord c, Widget parent, int ch, int prob, int[] acc, int max) {
	    super(c, imgsz(bg), parent);
	    this.ch = ch;
	    this.prob = prob;
	    this.acc = acc;
	    this.max = max;
	}

	public static WritableRaster evmeter(Raster tex, int val, int max) {
	    if (val < 0){val = 0;}
	    int w = (Math.min(val, max) * tex.getWidth()) / Math.max(max, 1);
	    WritableRaster bar = imgraster(imgsz(tex));
	    for(int y = 0; y < bar.getHeight(); y++) {
		for(int x = 0; x < bar.getWidth(); x++)
		    bar.setSample(x, y, 3, 255);
	    }
	    blit(bar, tex, Coord.z);
	    gayblit(bar, 3, new Coord(w - cap.getWidth(), 0), cap.getRaster(), 0, Coord.z);
	    for(int y = 0; y < bar.getHeight(); y++) {
		for(int x = w; x < bar.getWidth(); x++)
		    bar.setSample(x, y, 3, 0);
	    }
	    return(bar);
	}

	public static WritableRaster chm(int prob) {
	    BufferedImage mask = TexI.mkbuf(imgsz(chm));
	    Graphics g = mask.getGraphics();
	    Utils.AA(g);
	    g.fillArc(0, 0, mask.getWidth(), mask.getHeight(), 90, (-360 * prob) / 100);
	    g.dispose();
	    WritableRaster pm = copy(chm.getRaster());
	    gayblit(pm, 3, Coord.z, mask.getRaster(), 3, Coord.z);
	    return(pm);
	}

	public void draw(BufferedImage buf) {
	    WritableRaster dst = buf.getRaster();
	    blit(dst, bg.getRaster(), Coord.z);
	    alphablit(dst, titles[ch].getRaster(), new Coord((bg.getWidth() - titles[ch].getWidth()) / 2, 5));

	    for(int i = 0; i < bars.length; i++)
		alphablit(dst, evmeter(bars[i].getRaster(), acc[i], max), mc[i]);

	    alphablit(dst, chm(prob), chmc);
	    BufferedImage pt = chmf.renderf("%d%%", prob).img;
	    alphablit(dst, pt.getRaster(), chmc.add(imgsz(chm).sub(imgsz(pt)).div(2)));
	}
	
	public void draw(GOut g) {
	    if(alpha != 255)
		g.chcolor(255, 255, 255, alpha);
	    super.draw(g);
	}

	public class Discard extends NormAnim {
	    public Discard() {super(0.5);}

	    public void ntick(double a) {
		alpha = (int)(255 * (1.0 - a));
		if(a == 1.0)
		    destroy();
	    }
	}

	public class Trigger extends NormAnim {
	    Coord oc = c;

	    public Trigger() {super(1.25);}

	    public void ntick(double a) {
		Coord tc = new Coord(getparent(GameUI.class).sz.x / 2, 0).sub(sz.x / 2, sz.y);
		if(a < 0.25) {
		} else {
		    a = (a - 0.25) / 0.75;
		    c = new Coord((int)((c.x * (1.0 - a)) + (tc.x * a)), (int)((c.y * (1.0 - a)) + (tc.y * a)));
		}
		if(a == 1.0)
		    destroy();
	    }
	}
    }

    public Gobble(Coord c, Widget parent) {
	super(c, Utils.imgsz(Tempers.bg).add(0, vf.height() + 6), parent);
    }

    private GobbleInfo lfood;
    public void tick(double dt) {
	int max = 0;
	int[] lmax = new int[4];
	for(int i = 0; i < 4; i++) {
	    lmax[i] = ui.sess.glob.cattr.get(Tempers.anm[i]).comp;
	    if(lmax[i] == 0)
		return;
	    if(lmax[i] != this.lmax[i]){
		redraw();
		if(this.lmax[i] != 0){
		    ui.message(String.format("You have raised %s!", Tempers.rnm[i]));
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
	    if(ttevs != null) {
		for(Event ev : ttevs)
		    ev.destroy();
		ttevs = null;
	    }
	    if(food != null) {
		ttevs = new Event[4];
		final GameUI gui = getparent(GameUI.class);
		for(int i = 0; i < Alchemy.names.length; i++) {
		    final int ic = i;
		    ttevs[i] = new Event(Coord.z, gui, i, ch.a[i] / 100, food.evs[i], max) {
			    public void presize() {
				c = new Coord((gui.sz.x / 2) + (Event.bg.getWidth() * (ic - 2)), (gui.sz.y / 2) + 30);
			    }
			};
		    ttevs[i].alpha = 192;
		    ttevs[i].presize();
		}
	    }
	    lfood = food;
	}
    }

    public void draw(BufferedImage buf) {
	WritableRaster dst = buf.getRaster();
	blit(dst, bg.getRaster(), Coord.z);

	alphablit(dst, rmeter(sbars[0].getRaster(), lmax[0], max), mc[0]);
	alphablit(dst, lmeter(sbars[1].getRaster(), lmax[1], max), mc[1].sub(bars[1].getWidth() - 1, 0));
	alphablit(dst, lmeter(sbars[2].getRaster(), lmax[2], max), mc[2].sub(bars[2].getWidth() - 1, 0));
	alphablit(dst, rmeter(sbars[3].getRaster(), lmax[3], max), mc[3]);

	alphablit(dst, rmeter(bars[0].getRaster(), lev[0], max), mc[0]);
	alphablit(dst, lmeter(bars[1].getRaster(), lev[1], max), mc[1].sub(bars[1].getWidth() - 1, 0));
	alphablit(dst, lmeter(bars[2].getRaster(), lev[2], max), mc[2].sub(bars[2].getWidth() - 1, 0));
	alphablit(dst, rmeter(bars[3].getRaster(), lev[3], max), mc[3]);

	int cv = Math.min(var, 4);
	WritableRaster vt = vf.render("Invariance: " + var, new Color(Math.min(192 + (cv * 16), 255), Math.max(255 - (cv * 64), 0), 192 - (cv * 48))).img.getRaster();
	Coord vc = new Coord((sz.x - vt.getWidth()) / 2, boxsz.y + 5);
	for(int y = vc.y - 3; y < vc.y + vt.getHeight() + 6; y++) {
	    for(int x = vc.x - 3; x < vc.x + vt.getWidth() + 6; x++) {
		dst.setSample(x, y, 0, 0);
		dst.setSample(x, y, 1, 0);
		dst.setSample(x, y, 2, 0);
	    }
	}
	alphablit(dst, blurmask(vt, 2, 1, Color.BLACK), vc.sub(3, 3));
	alphablit(dst, vt, vc);

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

    private void cleareating() {
	if(cevs != null) {
	    for(Event ev : cevs)
		ev.destroy();
	    prog.destroy();
	    cevs = null;
	}
    }

    public void destroy() {
	cleareating();
	if(ttevs != null) {
	    for(Event ev : ttevs)
		ev.destroy();
	}
	super.destroy();
    }

    public void updt(int[] n) {
	this.lev = n;
	texts = null;
	redraw();
    }

    public void trig(int a) {
	for(int i = 0; i < cevs.length; i++) {
	    if(i == a)
		cevs[i].new Trigger();
	    else
		cevs[i].new Discard();
	}
	prog.destroy();
	cevs = null;
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

    public void updv(int v) {
	this.var = v;
	redraw();
    }

    public void eating(Object... args) {
	cleareating();
	if(args.length > 0) {
	    int a = 0;
	    Indir<Resource> res = ui.sess.getres((Integer)args[a++]);
	    int time = (Integer)args[a++];
	    boolean newt = ((Integer)args[a++]) != 0;
	    int[] probs = new int[4];
	    int[][] evs = new int[4][];
	    for(int i = 0; i < Alchemy.names.length; i++) {
		probs[i] = (Integer)args[a++] / 100;
		evs[i] = new int[4];
		for(int o = 0; o < anm.length; o++) {
		    evs[i][o] = (Integer)args[a++];
		}
	    }
	    cevs = new Event[4];
	    final GameUI gui = getparent(GameUI.class);
	    for(int i = 0; i < Alchemy.names.length; i++) {
		final int ic = i;
		cevs[i] = new Event(Coord.z, gui, i, probs[i], evs[i], max) {
			public void presize() {
			    c = new Coord((gui.sz.x / 2) + (Event.bg.getWidth() * (ic - 2)), (gui.sz.y / 2) - Event.bg.getHeight() - 30);
			}
		    };
		cevs[i].presize();
	    }
	    prog = new Progress(Coord.z, gui) {
		    public void presize() {
			c = gui.sz.sub(sz).div(2);
		    }
		};
	    prog.presize();
	}
    }

    public void prog(int p) {
	this.prog.ch(p);
    }

    public Object tooltip(Coord c, Widget prev) {
	if(!c.isect(boxc, boxsz))
	    return(null);
	return(super.tooltip(c, prev));
    }
}
