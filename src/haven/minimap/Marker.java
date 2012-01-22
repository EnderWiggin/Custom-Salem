package haven.minimap;

import haven.*;

import java.awt.*;

public class Marker {
    public final String name;
    public final Gob gob;
    public final MarkerTemplate template;
    private final int radius = 4;
    private final Tex tex = Utils.generateMarkerTex();

    public Marker(String name, Gob gob, MarkerTemplate template) {
        this.name = name;
        this.gob = gob;
        this.template = template;
    }
    
    public boolean hit(Coord c) {
        Coord3f ptc3f = gob.getc();
        if (ptc3f == null)
            return false;
        Coord p = new Coord((int)ptc3f.x, (int)ptc3f.y);
        return (c.x - p.x) * (c.x - p.x) + (c.y - p.y) * (c.y - p.y) < radius * radius * MCache.tilesz.x * MCache.tilesz.y;
    }

    public void draw(GOut g, Coord c) {
        Coord3f ptc3f = gob.getc();
        if (ptc3f == null)
            return;
        Coord ptc = new Coord((int)ptc3f.x, (int)ptc3f.y);
        ptc = ptc.div(MCache.tilesz).add(c);
        g.chcolor(template.color);
        g.image(tex, ptc.sub(tex.sz().div(2)));
        g.chcolor();
    }
}
