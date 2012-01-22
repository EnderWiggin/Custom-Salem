package haven;

import java.awt.event.KeyEvent;
import java.util.*;

class FlatnessTool extends Window implements MapView.Grabber {
    static final String title = "Area selection";
    static final String defaulttext = "Select area";
    private final Label text;
    private final MapView mv;
    boolean dm = false;
    Coord sc;
    Coord c1, c2;
    MCache.Overlay ol;
    final List<MCache.Overlay> lowestol = new ArrayList<MCache.Overlay>();
    final MCache map;

    public FlatnessTool(MapView mv, String text, Coord c, Widget parent) {
        super(c, new Coord(150, 50), parent, title);
        this.map = this.ui.sess.glob.map;
        this.text = new Label(Coord.z, this, defaulttext);
        this.mv = mv;
        this.mv.enol(16, 17);
        this.mv.grab(this);
        this.pack();
    }

    private void checkflatness(Coord c1, Coord c2) {
        if (c1.equals(this.c1) && c2.equals(this.c2))
            return;

        this.c1 = c1;
        this.c2 = c2;
        Area a = new Area(c1.y, c1.x, c2.y, c2.x);
        
        Coord[] tiles = a.coords();
        float[] heights = new float[tiles.length];
        
        boolean flat = true;
        float minheight = 0;
        float prevheight = Float.NaN;
        
        for (int i = 0; i < tiles.length; i++) {
            // find z-level value in the center of the tile
            heights[i] = map.getcz(tilify(tiles[i].mul(MCache.tilesz)));
            if (i == 0) {
                prevheight = heights[i];
                minheight = heights[i];
                flat = Math.abs(heights[i] - map.getz(tiles[i])) < 0.0001;
            } else if (Math.abs(heights[i] - prevheight) > 0.0001) {
                flat = false;
            }
            if (i != 0 && heights[i] < minheight) {
                minheight = heights[i];
            }
        }

        String text = "";
        clearlowestol();
        if (flat)
            text += "Area is flat.";
        else {
            makelowestol(tiles, heights, minheight);
            text += "Area isn't flat.";
        }
        text += " Lowest height: [" + minheight + "].";

        settext(text);

        this.pack();
    }

    public final void close() {
        this.cbtn.click();
    }

    private void clearlowestol() {
        for (MCache.Overlay ol : lowestol)
            ol.destroy();
        lowestol.clear();
    }

    private void makelowestol(Coord[] tiles, float[] heights, float height) {
        for (int i = 0; i < tiles.length; i++) {
            if (Math.abs(heights[i] - height) < 0.0001) {
                MCache.Overlay ol = map.new Overlay(tiles[i], tiles[i], 1<<17);
                lowestol.add(ol);
            }
        }
    }

    @Override
    public void destroy() {
        if (this.ol != null)
            this.ol.destroy();
        for (MCache.Overlay ol : lowestol)
            ol.destroy();
        this.mv.disol(16, 17);
        this.mv.release(this);
        super.destroy();
    }

    @Override
    public boolean mmousedown(Coord mc, int button) {
        Coord c = mc.div(MCache.tilesz);
        if (this.ol != null)
            this.ol.destroy();
        this.ol = map.new Overlay(c, c, 1<<16);
        this.sc = c;
        this.dm = true;
        this.ui.grabmouse(this.mv);
        
        checkflatness(c, c);
        
        return true;
    }

    @Override
    public boolean mmouseup(Coord mc, int button) {
        this.dm = false;
        this.ui.grabmouse(null);
        return true;
    }

    @Override
    public void mmousemove(Coord mc) {
        if (!this.dm)
            return;
        Coord c = mc.div(MCache.tilesz);
        Coord localCoord2 = new Coord(0, 0);
        Coord localCoord3 = new Coord(0, 0);
        if (c.x < this.sc.x) {
            localCoord2.x = c.x;
            localCoord3.x = this.sc.x;
        } else {
            localCoord2.x = this.sc.x;
            localCoord3.x = c.x;
        }
        if (c.y < this.sc.y) {
            localCoord2.y = c.y;
            localCoord3.y = this.sc.y;
        } else {
            localCoord2.y = this.sc.y;
            localCoord3.y = c.y;
        }
        this.ol.update(localCoord2, localCoord3);
        checkflatness(localCoord2, localCoord3);
    }
    
    @Override
    public void uimsg(String msg, Object... args) {
        if (msg == "reset") {
            this.ol.destroy();
            this.ol = null;
            this.c1 = (this.c2 = null);
        }
    }

    private static Coord tilify(Coord c) {
        c = c.div(MCache.tilesz);
        c = c.mul(MCache.tilesz);
        c = c.add(MCache.tilesz.div(2));
        return c;
    }

    public boolean type(char key, java.awt.event.KeyEvent ev) {
        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE) {
            close();
            return true;
        }
        return super.type(key, ev);
    }

    @Override
    public void wdgmsg(Widget wdg, String msg, Object... args) {
        if (wdg == cbtn) {
            ui.destroy(this);
        } else {
            super.wdgmsg(wdg, msg, args);
        }
    }

    private final void settext(String text) {
        this.text.settext(text);
    }
}
