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
import java.awt.image.*;
import static haven.PUtils.*;

public class Tempers extends SIWidget {
    public static final BufferedImage bg = Resource.loadimg("gfx/hud/tempers/bg");
    public static final BufferedImage[] bars, sbars, fbars;
    public static final BufferedImage lcap = Resource.loadimg("gfx/hud/tempers/lcap");
    public static final BufferedImage rcap = Resource.loadimg("gfx/hud/tempers/rcap");
    static final Color softc = new Color(64, 64, 64);
    static final Color foodc = new Color(128, 128, 0);
    static final Coord[] mc = {new Coord(293, 9), new Coord(293, 33), new Coord(233, 33), new Coord(233, 9)};
    static final Coord boxc = new Coord(96, 0), boxsz = new Coord(339, 62);
    static final String[] anm = {"blood", "phlegm", "ybile", "bbile"};
    static final String[] rnm = {"Blood", "Phlegm", "Yellow Bile", "Black Bile"};
    int[] soft = new int[4], hard = new int[4];
    int[] lmax = new int[4];
    boolean full = false;
    Tex tt = null;

    static {
	int n = anm.length;
	BufferedImage[] b = new BufferedImage[n];
	BufferedImage[] s = new BufferedImage[n];
	BufferedImage[] f = new BufferedImage[n];
	for(int i = 0; i < n; i++) {
	    b[i] = Resource.loadimg("gfx/hud/tempers/" + anm[i]);
	    s[i] = monochromize(b[i], softc);
	    f[i] = monochromize(b[i], foodc);
	}
	bars = b;
	sbars = s;
	fbars = f;
    }
    
    public Tempers(Coord c, Widget parent) {
	super(c, imgsz(bg), parent);
    }
    
    private FoodInfo lfood;
    public void tick(double dt) {
	int[] max = new int[4];
	full = true;
	for(int i = 0; i < 4; i++) {
	    max[i] = ui.sess.glob.cattr.get(anm[i]).comp;
	    if(max[i] == 0)
		return;
	    if(hard[i] < max[i])
		full = false;
	    if(max[i] != lmax[i]) {
		redraw();
		tt = null;
	    }
	}
	lmax = max;

	FoodInfo food = null;
	if(ui.lasttip instanceof WItem.ItemTip) {
	    try {
		food = ItemInfo.find(FoodInfo.class, ((WItem.ItemTip)ui.lasttip).item().info());
	    } catch(Loading e) {}
	}
	if(lfood != food) {
	    lfood = food;
	    redraw();
	}
    }

    private static WritableRaster rmeter(Raster tex, int val, int max) {
	int w = 1 + (val * (tex.getWidth() - 1)) / Math.max(max, 1);
	WritableRaster bar = copy(tex);
	gayblit(bar, 3, new Coord(w - rcap.getWidth(), 0), rcap.getRaster(), 0, Coord.z);
	for(int y = 0; y < bar.getHeight(); y++) {
	    for(int x = w; x < bar.getWidth(); x++)
		bar.setSample(x, y, 3, 0);
	}
	return(bar);
    }

    private static WritableRaster lmeter(Raster tex, int val, int max) {
	int w = 1 + (val * (tex.getWidth() - 1)) / Math.max(max, 1);
	WritableRaster bar = copy(tex);
	gayblit(bar, 3, new Coord(bar.getWidth() - w, 0), lcap.getRaster(), 0, Coord.z);
	for(int y = 0; y < bar.getHeight(); y++) {
	    for(int x = 0; x < bar.getWidth() - w; x++)
		bar.setSample(x, y, 3, 0);
	}
	return(bar);
    }

    private WritableRaster rfmeter(FoodInfo food, int t) {
	return(alphablit(rmeter(fbars[t].getRaster(), soft[t] + food.tempers[t], lmax[t]),
			 rmeter(sbars[t].getRaster(), soft[t], lmax[t]),
			 Coord.z));
    }

    private WritableRaster lfmeter(FoodInfo food, int t) {
	return(alphablit(lmeter(fbars[t].getRaster(), soft[t] + food.tempers[t], lmax[t]),
			 lmeter(sbars[t].getRaster(), soft[t], lmax[t]),
			 Coord.z));
    }

    public void draw(BufferedImage buf) {
	WritableRaster dst = buf.getRaster();
	blit(dst, bg.getRaster(), Coord.z);

	if(lfood != null) {
	    alphablit(dst, rfmeter(lfood, 0), mc[0]);
	    alphablit(dst, rfmeter(lfood, 1), mc[1]);
	    alphablit(dst, lfmeter(lfood, 2), mc[2].sub(bars[2].getWidth() - 1, 0));
	    alphablit(dst, lfmeter(lfood, 3), mc[3].sub(bars[3].getWidth() - 1, 0));
	} else {
	    if(soft[0] > hard[0]) alphablit(dst, rmeter(sbars[0].getRaster(), soft[0], lmax[0]), mc[0]);
	    if(soft[1] > hard[1]) alphablit(dst, rmeter(sbars[1].getRaster(), soft[1], lmax[1]), mc[1]);
	    if(soft[2] > hard[2]) alphablit(dst, lmeter(sbars[2].getRaster(), soft[2], lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
	    if(soft[3] > hard[3]) alphablit(dst, lmeter(sbars[3].getRaster(), soft[3], lmax[3]), mc[3].sub(bars[3].getWidth() - 1, 0));
	}

	alphablit(dst, rmeter(bars[0].getRaster(), hard[0], lmax[0]), mc[0]);
	alphablit(dst, rmeter(bars[1].getRaster(), hard[1], lmax[1]), mc[1]);
	alphablit(dst, lmeter(bars[2].getRaster(), hard[2], lmax[2]), mc[2].sub(bars[2].getWidth() - 1, 0));
	alphablit(dst, lmeter(bars[3].getRaster(), hard[3], lmax[3]), mc[3].sub(bars[3].getWidth() - 1, 0));
    }
    
    public void upds(int[] n) {
	this.soft = n;
	redraw();
	tt = null;
    }
    
    public void updh(int[] n) {
	this.hard = n;
	redraw();
	tt = null;
    }
    
    public Object tooltip(Coord c, Widget prev) {
	if(c.isect(boxc, boxsz)) {
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
	if(c.isect(boxc, boxsz)) {
	    getparent(GameUI.class).act("gobble");
	    return(true);
	}
	return(super.mousedown(c, button));
    }
}
