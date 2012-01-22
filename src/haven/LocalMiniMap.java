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

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;

import haven.MCache.Grid;
import haven.MCache.LoadingMap;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class LocalMiniMap extends Window {
    static Tex bg = Resource.loadtex("gfx/hud/bgtex");
    public static final Resource plx = Resource.load("gfx/hud/mmap/x");
    public final MapView mv;
    Tex mapimg = null;
    Coord ultile = null, cgrid = null;
    private final BufferedImage[] texes = new BufferedImage[256];
    private Coord off = new Coord();
    private Map<String, TexI> cache = new HashMap<String, TexI>();
    boolean rsm = false;
    boolean dm = false;
    private static Coord gzsz = new Coord(15,15);
    public int scale = 4;
    private static final Coord minsz = new Coord(125, 125);
    private static final double scales[] = {0.5, 0.66, 0.8, 0.9, 1, 1.25, 1.5, 1.75, 2};
    private Coord sp;
    private String session;
    
    private BufferedImage tileimg(int t) {
	BufferedImage img = texes[t];
	if(img == null) {
	    Resource r = ui.sess.glob.map.sets[t];
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
	MCache m = ui.sess.glob.map;
	BufferedImage buf = TexI.mkbuf(sz);
	Coord c = new Coord();
	for(c.y = 0; c.y < sz.y; c.y++) {
	    for(c.x = 0; c.x < sz.x; c.x++) {
		int t = m.gettile(ul.add(c));
		BufferedImage tex = tileimg(t);
		if(tex != null)
		    buf.setRGB(c.x, c.y, tex.getRGB(Utils.floormod(c.x + ul.x, tex.getWidth()),
						    Utils.floormod(c.y + ul.y, tex.getHeight())));
	    }
	}
	for(c.y = 0; c.y < sz.y; c.y++) {
	    for(c.x = 0; c.x < sz.x; c.x++) {
		int t = m.gettile(ul.add(c));
		try {
		    if((m.gettile(ul.add(c).add(-1, 0)) > t) ||
		       (m.gettile(ul.add(c).add( 1, 0)) > t) ||
		       (m.gettile(ul.add(c).add(0, -1)) > t) ||
		       (m.gettile(ul.add(c).add(0,  1)) > t))
		        buf.setRGB(c.x, c.y, Color.BLACK.getRGB());
		} catch (LoadingMap e) {
		    continue;
		}
	    }
	}
	return(buf);
    }

    public LocalMiniMap(Coord c, Coord sz, Widget parent, MapView mv) {
	super(c, sz, parent, "Minimap");
	this.mv = mv;
    }
    
    public void draw(GOut og) {
	Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
	if(pl == null)
	    return;
	Coord plt = pl.rc.div(tilesz);
	Coord plg = plt.div(cmaps);
	checkSession(plg);
	
	double scale = getScale();
	Coord hsz = sz.div(scale);
	
	Coord tc = plt.add(off.div(scale));
	Coord ulg = tc.div(cmaps);
	int dy = -tc.y + (hsz.y / 2);
	int dx = -tc.x + (hsz.x / 2);
	while((ulg.x * cmaps.x) + dx > 0)
	    ulg.x--;
	while((ulg.y * cmaps.y) + dy > 0)
	    ulg.y--;
	
	Coord s = bg.sz();
	for(int y = 0; (y * s.y) < sz.y; y++) {
	    for(int x = 0; (x * s.x) < sz.x; x++) {
		og.image(bg, new Coord(x*s.x, y*s.y));
	    }
	}
	
	GOut g = og.reclipl(og.ul.mul((1-scale)/scale), hsz);
	g.gl.glPushMatrix();
	g.gl.glScaled(scale, scale, scale);
	
	MCache m = this.ui.sess.glob.map;
	Coord cg = new Coord();
	
	for(cg.y = ulg.y; (cg.y * cmaps.y) + dy < hsz.y; cg.y++) {
	    for(cg.x = ulg.x; (cg.x * cmaps.x) + dx < hsz.x; cg.x++) {
		TexI img = cache.get(cg.toString());
		if(img == null){
		    Grid gr = m.grids.get(cg);
		    if(gr != null){
			img = new TexI(drawmap(gr.ul, cmaps));
			cache.put(cg.toString(), img);
			store(img.back, cg);


		    }
		}
		if(img == null){continue;}
		
		g.image(img, cg.mul(cmaps).add(tc.inv()).add(hsz.div(2)));
	    }
	}
	Coord c0 = hsz.div(2).sub(tc);
	synchronized(ui.sess.glob.party.memb) {
		for(Party.Member memb : ui.sess.glob.party.memb.values()) {
		    Coord ptc = memb.getc();
		    if(ptc == null)
			continue;
		    ptc = c0.add(ptc.div(tilesz));
//		    g.chcolor(memb.col.getRed(), memb.col.getGreen(), memb.col.getBlue(), 128);
		    g.chcolor(memb.col);
		    g.image(plx.layer(Resource.imgc).tex(), ptc.add(plx.layer(Resource.negc).cc.inv()));
		    //g.fellipse(ptc, new Coord(10,10));
		    g.chcolor();
		}
	    }
	
	g.gl.glPopMatrix();
	Window.swbox.draw(og, Coord.z, this.sz);

    }
    
    private void store(BufferedImage img, Coord cg) {
	Coord c = cg.sub(sp);
	String fileName = String.format("map/%s/tile_%d_%d.png", session, c.x, c.y);
	File outputfile = new File(fileName);
	try {
	    ImageIO.write(img, "png", outputfile);
	} catch (IOException e) {}
    }

    private void checkSession(Coord plg) {
	if(cgrid == null || plg.manhattan(cgrid) > 5){
	    sp = plg;
	    cache.clear();
	    session = (new SimpleDateFormat("yyyy-MM-dd HH.mm.ss")).format(new Date(System.currentTimeMillis()));
	    (new File("map/" + session)).mkdirs();
	    try {
		Writer currentSessionFile = new FileWriter("map/currentsession.js");
		currentSessionFile.write("var currentSession = '" + session + "';\n");
		currentSessionFile.close();
	    } catch (IOException e) {}
	}
	cgrid = plg;
    }

    public double getScale() {
        return scales[scale];
    }

    public void setScale(int scale) {
	this.scale = Math.max(0,Math.min(scale,scales.length-1));
    }
    
    public boolean mousedown(Coord c, int button) {
//	if(folded) {
//	    return super.mousedown(c, button);
//	}
	
	parent.setfocus(this);
	raise();
	
	if(button == 3){
	    dm = true;
	    ui.grabmouse(this);
	    doff = c;
	    return true;
	}
	
	if (button == 1) {
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
//	if(dm){
//	    Config.setWindowOpt("minimap_pos", this.c.toString());
//	}
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
//	    Config.setWindowOpt("minimap_sz", mm.sz.toString());
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
	    //pack();
	} else {
	    super.mousemove(c);
	}
    }

    @Override
    public boolean mousewheel(Coord c, int amount) {
	if( amount > 0){
	    setScale(scale - 1);
	} else {
	    setScale(scale + 1);
	}
	return true;
    }
}
