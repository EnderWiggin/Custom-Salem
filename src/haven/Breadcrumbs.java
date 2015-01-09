package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public abstract class Breadcrumbs extends Widget {
    private static final Coord border = new Coord(2,2);
    private static final BufferedImage separator = Resource.loadimg("gfx/hud/breadcrumb");
    private final Coord SZ;
    private List<Crumb> crumbs;
    private List<IButton> buttons;

    public Breadcrumbs(Coord c, Coord sz, Widget parent) {
	super(c, sz.add(0, border.x*2), parent);
	int d = sz.y;
	SZ = new Coord(d, d);
	buttons = new LinkedList<IButton>();
    }

    public void setCrumbs(List<Crumb> crumbs){
	this.crumbs = crumbs;
	cleanButtons();
	createButtons();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if (sender instanceof IButton && buttons.contains(sender)) {
	    if(msg.equals("activate")){
		int k = buttons.indexOf(sender);
		selected(crumbs.get(k).data);
	    }
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }

    public abstract void selected(Object data);

    @Override
    public void draw(GOut g) {
	for(int i = 1; i<buttons.size(); i++){
	    g.image(separator,buttons.get(i).c.sub(12,-2));
	}
	super.draw(g);
    }

    private void createButtons() {
	int w = 0;
	for(Crumb item : crumbs){
	    BufferedImage txt = Text.render(item.text).img;

	    Coord isz = Utils.imgsz(txt).add(border.x*3+SZ.x,0);
	    int ty = (sz.y - isz.y)/2;
	    isz.y = sz.y;

	    BufferedImage up = TexI.mkbuf(isz);
	    Graphics g = up.getGraphics();
	    g.drawImage(item.img, border.x, border.y, SZ.x, SZ.y,null);
	    g.drawImage(txt, SZ.x+border.x*2, ty, null);

	    BufferedImage down = highlight(up, new Color(0x44EFE40A, true));
	    BufferedImage over = highlight(up, new Color(0x44C5C3BD, true));

	    IButton btn = new IButton(new Coord(w, 0), this, up, down, over);
	    btn.recthit = true;
	    buttons.add(btn);

	    w += isz.x + 14;
	}
    }

    private BufferedImage highlight(BufferedImage img, Color color) {
	Coord imgsz = Utils.imgsz(img);
	BufferedImage ret = TexI.mkbuf(imgsz);
	Graphics g = ret.getGraphics();
	g.drawImage(img, 0, 0, color, null);
	return ret;
    }

    private void cleanButtons() {
	for(IButton btn : buttons){
	    ui.destroy(btn);
	}
	buttons.clear();
    }

    public static class Crumb{
	public BufferedImage img;
	public String text;
	public Object data;

	public Crumb(BufferedImage img, String text, Object data){
	    this.img = img;
	    this.text = text;
	    this.data = data;
	}
    }
}
