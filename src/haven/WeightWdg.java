package haven;

import java.awt.*;
import java.awt.event.KeyEvent;

public class WeightWdg extends Window {
    static final Tex bg = Resource.loadtex("gfx/hud/bgtex");

    private Tex label;

    public WeightWdg(Coord c, Widget parent) {
	super(c,  Coord.z, parent, "weightwdg");
	this.cap = null;
	sz = new Coord(100, 30);
    }

    public void update(int weight){
	if(label != null){
	    label.dispose();
	}

	int cap = 25000;
	Glob.CAttr ca = ui.sess.glob.cattr.get("carry");
	if(ca != null)
	    cap = ca.comp;
	Color color = (weight > cap)? Color.RED:Color.WHITE;

	label = Text.render(String.format("Weight: %.2f/%.2f kg", weight / 1000.0, cap / 1000.0), color).tex();
	sz = label.sz().add(Window.swbox.bisz()).add(4,0);
    }

    @Override
    public void tick(double dt) {
	if(Config.weight_wdg != visible){
	    show(Config.weight_wdg);
	}
    }

    @Override
    public void draw(GOut g) {
	Coord s = bg.sz();
	for(int y = 0; (y * s.y) < sz.y; y++) {
	    for(int x = 0; (x * s.x) < sz.x; x++) {
		g.image(bg, new Coord(x * s.x, y * s.y));
	    }
	}

	if(label != null){
	    g.aimage(label, sz.div(2), 0.5, 0.5);
	}

	g.chcolor(SeasonImg.color);
	Window.swbox.draw(g, Coord.z, this.sz);
	g.chcolor();
    }

    @Override
    public boolean type(char key, KeyEvent ev) {
	return false;
    }
}
