package haven;

import java.awt.image.BufferedImage;

import org.ender.wiki.HtmlDraw;
import org.ender.wiki.Item;
import org.ender.wiki.Wiki;
import org.ender.wiki.Request.Callback;


public class WikiPage extends SIWidget implements Callback {
    private HtmlDraw hd;
    private String name = "Ore Smelter";
    BufferedImage img;
    private long last = 1;
    int count = 0;
    public WikiPage(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent);
	Wiki.get(name, this);
    }
    @Override
    public void draw(GOut g) {
	long now = System.currentTimeMillis();
	if(now - last > 1000 && count < 10){
	    last = now;
	    count++;
	    redraw();
	}
	super.draw(g);
    }
    
    @Override
    public void draw(BufferedImage buf){
	if(!visible){return;}
	if(hd == null){return;}
	hd.get(buf, sz.x, sz.y);
	img = buf;
    }

    @Override
    public void wiki_item_ready(Item item) {
	if(item == null){return;}
	hd = new HtmlDraw(item.content);
	hd.setWidth(sz.x);
	sz.y = hd.getHeight();
	if(parent instanceof Scrollport.Scrollcont){
	    ((Scrollport.Scrollcont)parent).update();
	}
	redraw();
    }
}
