package haven;

import java.awt.image.BufferedImage;

import org.ender.wiki.HtmlDraw;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;


public class WikiPage extends SIWidget {
    private HtmlDraw hd;
    private String name = "Ore Smelter";
    BufferedImage img;
    private long last = 1;
    public WikiPage(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
	//init();
    }
    @Override
    public void draw(GOut g) {
	if(hd == null){init();}
	super.draw(g);
    }
    
    @Override
    public void draw(BufferedImage buf){
	if(hd == null){return;}
	hd.get(buf, sz.x, sz.y);
	img = buf;
	System.out.println("WP drawing...");
    }

    private void init(){
	Item item = Wiki.get(name);
	if(item == null){return;}
	hd = new HtmlDraw(item.content);
	hd.setWidth(sz.x);
	sz.y = hd.getHeight();
	if(parent instanceof Scrollport.Scrollcont){
	    ((Scrollport.Scrollcont)parent).update();
	}
	System.out.println("WP REDRAW...");
	redraw();
    }
}
