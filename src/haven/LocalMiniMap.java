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
import haven.Defer.Future;
import haven.MCache.LoadingMap;
import haven.Resource.Loading;
import haven.minimap.Marker;
import haven.minimap.Radar;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class LocalMiniMap extends Window implements Console.Directory{
    private static final SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    static Tex bg = Resource.loadtex("gfx/hud/bgtex");
    public static final Resource plx = Resource.load("gfx/hud/mmap/x");
    public final MapView mv;
    private Widget mapmenu;
    private Coord cgrid = null;
    private final BufferedImage[] texes = new BufferedImage[256];
    private Coord off = new Coord();
    boolean rsm = false;
    boolean dm = false;
    private static Coord gzsz = new Coord(15,15);
    public int scale = 4;
    private static final Coord minsz = new Coord(125, 125);
    private static final double scales[] = {0.5, 0.66, 0.8, 0.9, 1, 1.25, 1.5, 1.75, 2};
    private Coord sp;
    private String session;
    private final Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();
    private boolean radarenabled = true;
    private int height = 0;
    private Future<BufferedImage> heightmap;
    private Coord lastplg;
    private final Coord hmsz = cmaps.mul(3);
    
    private final Map<Coord, Future<MapTile>> cache = new LinkedHashMap<Coord, Defer.Future<MapTile>>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;

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

    private BufferedImage tileimg(int t) {
	BufferedImage img = texes[t];
	if(img == null) {
	    Resource r = ui.sess.glob.map.sets[t];
	    if(r == null)
		return(null);
	    try{
		Resource.Image ir = r.layer(Resource.imgc);
		if(ir == null)
		    return(null);
		img = ir.img;
		texes[t] = img;
	    }catch (Loading e){
		return null;
	    }
	}
	return(img);
    }
    
    public BufferedImage drawmap(Coord ul, Coord sz) {
	MCache m = ui.sess.glob.map;
	BufferedImage buf = TexI.mkbuf(sz);
	Coord c = new Coord();
	for(c.y = 0; c.y < sz.y; c.y++) {
	    for(c.x = 0; c.x < sz.x; c.x++) {
		Coord c2 = ul.add(c);
		int t;
		try{
		    t = m.gettile(c2);
		} catch (LoadingMap e) {
		    return null;
		}
		BufferedImage tex = tileimg(t);
		if(tex != null){
		    buf.setRGB(c.x, c.y, tex.getRGB(Utils.floormod(c.x + ul.x, tex.getWidth()),
						    Utils.floormod(c.y + ul.y, tex.getHeight())));
		} else {
		    return null;
		}
		
		try {
		    if((m.gettile(c2.add(-1, 0)) > t) ||
			(m.gettile(c2.add( 1, 0)) > t) ||
			(m.gettile(c2.add(0, -1)) > t) ||
			(m.gettile(c2.add(0,  1)) > t))
			buf.setRGB(c.x, c.y, Color.BLACK.getRGB());
		} catch (LoadingMap e) {
		    continue;
		}
	    }
	}
	return(buf);
    }
    
    private Future<BufferedImage> getheightmap(final Coord plg){
	Future<BufferedImage> f = Defer.later(new Defer.Callable<BufferedImage> () {
	    public BufferedImage call() {
		return drawmap2(plg);
	    }
	});
	return f;
    }
    
    public BufferedImage drawmap2(Coord plg) {
	MCache m = ui.sess.glob.map;
	Coord ul = (plg.sub(1, 1)).mul(cmaps);
	BufferedImage buf = TexI.mkbuf(hmsz);
	Coord c = new Coord();
	int MAX = Integer.MIN_VALUE;
	int MIN = Integer.MAX_VALUE;
	
	try{
	    for(c.y = 0; c.y < hmsz.y; c.y++) {
		for(c.x = 0; c.x < hmsz.x; c.x++) {
		    Coord c2 = ul.add(c);
		    int t = m.getz(c2);
		    if(t > MAX) {MAX = t;}
		    if(t < MIN) {MIN = t;}
		}
	    }
	} catch (LoadingMap e) {
	    return null;
	}
	
	int SIZE = MAX - MIN;
	
	for(c.y = 0; c.y < hmsz.y; c.y++) {
	    for(c.x = 0; c.x < hmsz.x; c.x++) {
		Coord c2 = ul.add(c);
		int t2 = m.getz(c2);
		int t = Math.max(t2, MIN);
		t = Math.min(t,  MAX);
		t = t - MIN;
		t = (255*t)/SIZE;
		t = t|(t<<8)|(t<<16)|height;
		buf.setRGB(c.x, c.y, t);
		try {
		    if((m.getz(c2.add(-1, 0)) > (t2+11)) ||
		       (m.getz(c2.add( 1, 0)) > (t2+11)) ||
		       (m.getz(c2.add(0, -1)) > (t2+11)) ||
		       (m.getz(c2.add(0,  1)) > (t2+11)))
		        buf.setRGB(c.x, c.y, Color.RED.getRGB());
		} catch (LoadingMap e) {
		    continue;
		}
	    }
	}
	return(buf);
    }

    public LocalMiniMap(Coord c, Coord sz, Widget parent, MapView mv, Widget mapmenu) {
	super(c, sz, parent, "Minimap");
	this.mapmenu = mapmenu;
	this.mv = mv;
	loadOpts();
	cmdmap.put("radar", new Console.Command() {
            public void run(Console console, String[] args) throws Exception {
                if (args.length == 2) {
                    String arg = args[1];
                    if (arg.equals("on")) {
                        radarenabled = true;
                        return;
                    }
                    else if (arg.equals("off")) {
                        radarenabled = false;
                        return;
                    }
                    else if (arg.equals("reload")) {
                        ui.sess.glob.oc.radar.reload();
                        return;
                    }
                }
                throw new Exception("No such setting");
            }
        });
    }
    
    private void loadOpts() {
	synchronized (Config.window_props) {
	    c = new Coord(Config.window_props.getProperty("mmap_pos", c.toString()));
	    sz = new Coord(Config.window_props.getProperty("mmap_sz", sz.toString()));
	    mapmenu.c = c.add(0,-18);
	}
    }
    
    public void toggleHeight(){
	if(height == 0){
	    height = 0xb5000000;
	} else if(height == 0xb5000000){
	    height = 0xff000000;
	} else {
	    height = 0;
	}
	heightmap = null;
    }
    
    public void draw(GOut og) {
	Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
	if(pl == null)
	    return;
	final Coord plt = pl.rc.div(tilesz);
	final Coord plg = plt.div(cmaps);
	checkSession(plg);
	if(!plg.equals(lastplg)){
	    lastplg = plg;
	    heightmap = null;
	}
	if((height!=0) && (heightmap == null)){
	    heightmap = getheightmap(plg);
	}
	
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
	
	Coord cg = new Coord();
	synchronized(cache) {
	    for(cg.y = ulg.y; (cg.y * cmaps.y) + dy < hsz.y; cg.y++) {
		for(cg.x = ulg.x; (cg.x * cmaps.x) + dx < hsz.x; cg.x++) {

		    Defer.Future<MapTile> f = cache.get(cg);
		    final Coord tcg = new Coord(cg);
		    final Coord ul = cg.mul(cmaps);
		    if((f == null) && (cg.manhattan2(plg) <= 1)) {
			f = Defer.later(new Defer.Callable<MapTile> () {
			    public MapTile call() {
				BufferedImage img = drawmap(ul, cmaps);
				if(img == null){return null;}
				store(img, tcg);
				return(new MapTile(new TexI(img), ul, tcg));
			    }
			});
			cache.put(tcg, f);
		    }
		    if((f == null) || (!f.done())) {
			continue;
		    }
		    MapTile mt = f.get();
		    if(mt == null){
			cache.put(cg, null);
			continue;
		    }
		    Tex img = mt.img;
		    g.image(img, ul.add(tc.inv()).add(hsz.div(2)));
		}
	    }
	}
	Coord c0 = hsz.div(2).sub(tc);
	
	if((height!=0) && (heightmap != null) && heightmap.done()){
	    BufferedImage img = heightmap.get();
	    if(img != null){
		g.image(img, c0.add(plg.sub(1,1).mul(cmaps)));
	    } else {
		heightmap = null;
	    }
	}
	
	drawmarkers(g, c0);
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
	String fileName = String.format("%s/map/%s/tile_%d_%d.png",Config.userhome, session, c.x, c.y);
	File outputfile = new File(fileName);
	try {
	    ImageIO.write(img, "png", outputfile);
	} catch (IOException e) {}
    }

    private void checkSession(Coord plg) {
	if(cgrid == null || plg.manhattan(cgrid) > 5){
	    sp = plg;
	    cache.clear();
	    session = datef.format(new Date(System.currentTimeMillis()));
	    (new File(Config.userhome+"/map/" + session)).mkdirs();
	    try {
		Writer currentSessionFile = new FileWriter(Config.userhome+"/map/currentsession.js");
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

	Marker m = getmarkerat(c);
	Coord mc = uitomap(c);

	if(button == 3){
	    if (m != null) {
                mv.wdgmsg("click", this.c.add(c), mc, button, ui.modflags(), (int)m.gob.id, m.gob.rc, (-1));
                return true;
            }
	    
	    dm = true;
	    ui.grabmouse(this);
	    doff = c;
	    return true;
	}
	
	if (button == 1) {
	    if (m != null || ui.modctrl) {
                mv.wdgmsg("click", Coord.z, mc, button, 0);
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
	if(super.dm){
	    Config.setWindowOpt("mmap_pos", this.c.toString());
	}
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
	    Config.setWindowOpt("mmap_sz", sz.toString());
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
	    mapmenu.c = this.c.add(0,-18);
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
    
    private void drawmarkers(GOut g, Coord tc) {
        if (!radarenabled)
            return;

        Radar radar = ui.sess.glob.oc.radar;
        try {
            for (Marker m : radar.getMarkers()) {
                if (m.template.visible)
                    m.draw(g, tc);
            }
        } catch (MCache.LoadingMap e) {
        }
    }
    
    private Coord uitomap(Coord c) {
        return c.sub(sz.div(2)).add(off).div(getScale()).mul(MCache.tilesz).add(mv.cc);
    }
    
    private Marker getmarkerat(Coord c) {
        if (radarenabled) {
            Radar radar = ui.sess.glob.oc.radar;
            try {
                Coord mc = uitomap(c);
                for (Marker m : radar.getMarkers()) {
                    if (m.template.visible && m.hit(mc))
                        return m;
                }
            } catch (MCache.LoadingMap e) {
            }
        }
        return null;
    }

    @Override
    public Object tooltip(Coord c, boolean again) {
        Marker m = getmarkerat(c);
        if (m != null)
            return m.template.tooltip;
        return null;
    }

    @Override
    public Map<String, Console.Command> findcmds() {
        return cmdmap;
    }

    @Override
    public boolean type(char key, KeyEvent ev) {
	if(key == 27) {
	    return false;
	}
	return super.type(key, ev);
    }
}
