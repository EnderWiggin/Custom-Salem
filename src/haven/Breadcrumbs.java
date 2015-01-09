package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class Breadcrumbs extends Widget {
    private static final Coord border = new Coord(2,2);
    private final Coord SZ;
    private List<Crumb> steps;
    private List<IButton> buttons;

    public Breadcrumbs(Coord c, Coord sz, Widget parent) {
	super(c, sz.add(0, border.x*2), parent);
	SZ = new Coord(sz.y, sz.y);
	buttons = new LinkedList<IButton>();
    }

    public void setSteps(List<Crumb> steps){
	this.steps = steps;
	cleanButtons();
	createButtons();
    }

    private void createButtons() {
	for(Crumb item : steps){
	    BufferedImage txt = Text.render(item.text).img;

	    Coord isz = Utils.imgsz(txt).add(border.x*3+SZ.x,0);
	    int ty = (sz.y - isz.y)/2;
	    isz.y = sz.y;

	    BufferedImage up = TexI.mkbuf(isz);
	    Graphics g = up.getGraphics();
	    g.drawImage(item.img, border.x, border.y, SZ.x, SZ.y,null);
	    g.drawImage(txt, SZ.x+border.x*2, ty, null);

	    BufferedImage down = highlight(up, new Color(0x447CD816, true));
	    BufferedImage over = highlight(up, new Color(0x44EFE40A, true));

	    IButton btn = new IButton(Coord.z, this, up, down, over);
	    btn.recthit = true;
	    buttons.add(btn);
	}
    }

    private BufferedImage highlight(BufferedImage img, Color color) {
	Coord imgsz = Utils.imgsz(img);
	BufferedImage ret = TexI.mkbuf(imgsz);
	Graphics g = ret.getGraphics();
	g.setColor(color);
	g.fillRect(0, 0, imgsz.x, imgsz.y);
	g.drawImage(img, 0, 0, null);
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
