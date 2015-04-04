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

import haven.MCache.LoadingMap;
import static haven.MCache.cmaps;
import static haven.MCache.tilesz;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import haven.resutil.RidgeTile;

public class LocalMiniMap extends Window {
    public final MapView mv;
    private Coord cc = null;
    private MapTile cur = null;
    private final Map<Coord, Defer.Future<MapTile>> cache = new LinkedHashMap<Coord, Defer.Future<MapTile>>(9, 0.75f, true) {
	protected boolean removeEldestEntry(Map.Entry<Coord, Defer.Future<MapTile>> eldest) {
	    if(size() > 75) {
		try {
		    MapTile t = eldest.getValue().get();
		    t.img.dispose();
		} catch(RuntimeException e) {
		}
		return(true);
	    }
	    return(false);
	}
    };
    
    public static class MapTile {
	public final Tex img;
	public final Coord ul, c;
	
	public MapTile(Tex img, Coord ul, Coord c) {
	    this.img = img;
	    this.ul = ul;
	    this.c = c;
	}
    }

    private BufferedImage tileimg(int t, BufferedImage[] texes) throws Loading{
	BufferedImage img = texes[t];
	if(img == null) {
	    Resource r = ui.sess.glob.map.tilesetr(t);
	    if(r == null)
		return(null);
	    Resource.Image ir = r.layer(Resource.imgc);
	    if(ir == null)
		return(null);
	    img = ir.img;
	    texes[t] = img;
	}
	return(img);
    }
    
    public BufferedImage drawmap(Coord ul, Coord sz) {
	BufferedImage[] texes = new BufferedImage[256];
	MCache m = ui.sess.glob.map;
	BufferedImage buf = TexI.mkbuf(sz);
	Coord c = new Coord();
	for(c.y = 0; c.y < sz.y; c.y++) {
	    for(c.x = 0; c.x < sz.x; c.x++) {
		Coord c2 = ul.add(c);
		int t;
		try{
		    t = m.gettile(c2);
		} catch (LoadingMap e){
		    return null;
		}
		try{
		    BufferedImage tex = tileimg(t, texes);
		    int rgb = 0;
		    if(tex != null){
			rgb = tex.getRGB(Utils.floormod(c.x, tex.getWidth()), Utils.floormod(c.y, tex.getHeight()));
		    }
		    buf.setRGB(c.x, c.y, rgb);
		} catch(Loading e){
		    return null;
		}
		try{
		    if((m.gettile(c2.add(-1, 0)) > t) ||
		       (m.gettile(c2.add(1, 0)) > t) ||
		       (m.gettile(c2.add(0, -1)) > t) ||
		       (m.gettile(c2.add(0, 1)) > t))
			buf.setRGB(c.x, c.y, Color.BLACK.getRGB());
		} catch (LoadingMap e){
		    continue;
		}
	       
	    }
	}
	 
	drawRidges(ul, sz, m, buf, c);
	return(buf);
    }

    private static void drawRidges(Coord ul, Coord sz, MCache m, BufferedImage buf, Coord c) {
        for(c.y = 1; c.y < sz.y - 1; c.y++) {
	    for(c.x = 1; c.x < sz.x - 1; c.x++) {
		int t = m.gettile(ul.add(c));
		Tiler tl = m.tiler(t);
		if(tl instanceof RidgeTile) {
		    if(((RidgeTile)tl).ridgep(m, ul.add(c))) {
			for(int y = c.y; y <= c.y + 1; y++) {
			    for(int x = c.x; x <= c.x + 1; x++) {
				int rgb = buf.getRGB(x, y);
				rgb = (rgb & 0xff000000) |
				    (((rgb & 0x00ff0000) >> 17) << 16) |
				    (((rgb & 0x0000ff00) >> 9) << 8) |
				    (((rgb & 0x000000ff) >> 1) << 0);
				buf.setRGB(x, y, rgb);
			    }
			}
		    }
		}
	    }
        }
    }

    public LocalMiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
	super(c, sz, parent, "mmap");
	this.mv = mv;
    }
    
    public Coord p2c(Coord pc) {
	Coord cc = this.cc.add(off);
	return(pc.div(tilesz).sub(cc).add(sz.div(2)));
    }

    public Coord c2p(Coord c) {
	Coord cc = this.cc.add(off);
	return(c.sub(sz.div(2)).add(cc).mul(tilesz).add(tilesz.div(2)));
    }

    public void drawicons(GOut g) {
	OCache oc = ui.sess.glob.oc;
	synchronized(oc) {
	    for(Gob gob : oc) {
		try {
		    GobIcon icon = gob.getattr(GobIcon.class);
		    if(icon != null) {
			Coord gc = p2c(gob.rc);
			Tex tex = icon.tex();
			g.image(tex, gc.sub(tex.sz().div(2)));
		    }
		} catch(Loading l) {}
	    }
	}
    }

    public Gob findicongob(Coord c) {
	OCache oc = ui.sess.glob.oc;
	synchronized(oc) {
	    for(Gob gob : oc) {
		try {
		    GobIcon icon = gob.getattr(GobIcon.class);
		    if(icon != null) {
			Coord gc = p2c(gob.rc);
			Coord sz = icon.tex().sz();
			if(c.isect(gc.sub(sz.div(2)), sz))
			    return(gob);
		    }
		} catch(Loading l) {}
	    }
	}
	return(null);
    }

    public void tick(double dt) {
	Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
	if(pl == null) {
	    this.cc = null;
	    return;
	}
	this.cc = pl.rc.div(tilesz);
    }

    public void draw(GOut g) {
	if(cc == null)
	    return;
	final Coord plg = cc.div(cmaps);
	Coord cc = this.cc.add(off);
	Coord ulg = cc.div(cmaps);
	int dy = -cc.y + (sz.y / 2);
	int dx = -cc.x + (sz.x / 2);
	while((ulg.x * cmaps.x) + dx > 0)
	    ulg.x--;
	while((ulg.y * cmaps.y) + dy > 0)
	    ulg.y--;

	Coord cg = new Coord();
	synchronized(cache){
	    for(cg.y = ulg.y; (cg.y * cmaps.y) + dy < sz.y; cg.y++){
		for(cg.x = ulg.x; (cg.x * cmaps.x) + dx < sz.x; cg.x++) {
		    Defer.Future<MapTile> f = cache.get(cg);
		    final Coord tcg = new Coord(cg);
		    final Coord ul = cg.mul(cmaps);
		    Coord diff = cg.sub(plg).abs();
		    if((f == null) && (Math.max(diff.x,diff.y) <= 1)){
			f = Defer.later(new Defer.Callable<MapTile> () {
			    public MapTile call() {
				BufferedImage img = drawmap(ul, cmaps);
				if(img == null){return null;}
				return(new MapTile(new TexI(img), ul, tcg));
			    }
			});
		    cache.put(tcg, f);
		    }
		    if((f == null || (!f.done()))){
			continue;
		    }
		    MapTile mt = f.get();
		    if(mt == null){
			cache.put(cg, null);
			continue;
		    }
		    Tex img = mt.img;
		    g.image(img, ul.add(cc.inv()).add(sz.div(2)));
		}
	    }
	}
	Coord c0 = sz.div(2).sub(cc);
	synchronized(ui.sess.glob.party.memb){
	    try {
		Tex tx = MiniMap.plx.layer(Resource.imgc).tex();
		Coord negc = MiniMap.plx.layer(Resource.negc).cc;
		for(Party.Member memb : ui.sess.glob.party.memb.values()){
		    Coord ptc = memb.getc();
		    if(ptc == null)
			continue;
		    ptc = c0.add(ptc.div(tilesz));
		    g.chcolor(memb.col);
		    g.image(tx, ptc.sub(negc));
		    g.chcolor();
		}
	    } catch(Loading e) {}
	}
	drawicons(g);
	Window.swbox.draw(g, Coord.z, this.sz);
    }

    boolean dm,rsm;
    Coord gzsz = new Coord(15,15);
    Coord minsz = new Coord(125,125);
    Coord off = new Coord(0,0),doff;
    
    private Coord uitomap(Coord c) {
	return c.sub(sz.div(2)).add(off).mul(MCache.tilesz).add(mv.cc);
    }
    
    public boolean mousedown(Coord c, int button) {
	parent.setfocus(this);
	raise();

	Coord mc = uitomap(c);
        
        Gob gob = findicongob(c);
        if(gob != null)
	    {
		mv.wdgmsg("click", rootpos().add(c), mc, button, ui.modflags(), 0, (int) gob.id, gob.rc, 0, -1);
		return true;
	    }
        
	if(button == 3){            
	    dm = true;
	    ui.grabmouse(this);
	    doff = c;
	    return true;
	}

	if (button == 1) {
	    if (ui.modctrl) {
		mv.wdgmsg("click", rootpos().add(c), mc, button, 0);
		return true;
	    }

	    ui.grabmouse(this);
	    doff = c;
	    if(c.isect(sz.sub(gzsz), gzsz)) {
		rsm = true;
		return true;
	    }
	}
	return super.mousedown(c, button);
    }

    public boolean mouseup(Coord c, int button) {
	if(button == 2){
	    off.x = off.y = 0;
	    return true;
	}

	if(button == 3){
	    dm = false;
	    ui.grabmouse(null);
	    return true;
	}

	if (rsm){
	    ui.grabmouse(null);
	    rsm = false;
	} else {
	    super.mouseup(c, button);
	}
	return (true);
    }

    public void mousemove(Coord c) {
	Coord d;
	if(dm){
	    d = c.sub(doff);
	    off = off.sub(d);
	    doff = c;
	    return;
	}

	if (rsm){
	    d = c.sub(doff);
	    sz = sz.add(d);
	    sz.x = Math.max(minsz.x, sz.x);
	    sz.y = Math.max(minsz.y, sz.y);
	    doff = c;
	} else {
	    super.mousemove(c);
	}
    }
}
